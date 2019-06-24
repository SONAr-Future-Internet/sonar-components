package br.ufu.facom.mehar.sonar.interceptor.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import br.ufu.facom.mehar.sonar.client.ndb.configuration.SonarProperties;
import br.ufu.facom.mehar.sonar.client.ndb.service.CoreDataService;
import br.ufu.facom.mehar.sonar.client.ndb.service.PropertyDataService;
import br.ufu.facom.mehar.sonar.client.nem.action.NetworkEventAction;
import br.ufu.facom.mehar.sonar.client.nem.configuration.SonarTopics;
import br.ufu.facom.mehar.sonar.client.nem.service.EventService;
import br.ufu.facom.mehar.sonar.core.model.core.Controller;
import br.ufu.facom.mehar.sonar.core.model.event.ControllerInterceptionEvent;
import br.ufu.facom.mehar.sonar.core.util.IPUtils;
import br.ufu.facom.mehar.sonar.core.util.ObjectUtils;
import br.ufu.facom.mehar.sonar.core.util.openflow.PacketOutRequest;
import br.ufu.facom.mehar.sonar.interceptor.manager.SonarSyncInterceptor;
import br.ufu.facom.mehar.sonar.interceptor.proxy.Connection;

@Component
public class ControllerProxyServer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerProxyServer.class);

	@Value("${interceptor.proxy.port:6600}")
	private Integer port;
	
	@Value("${sonar.ci.instance:192.168.0.1:6600}")
	private String instance;
	
	@Autowired
	private CoreDataService coreService;
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private PropertyDataService dataService;
	
	private Map<String,List<Socket>> deviceSockMap = new HashMap<String, List<Socket>>();
	
	@EventListener(ApplicationReadyEvent.class)
	public void providePacketOut() {
		LOGGER.info("Listening for events of topic '"+SonarTopics.TOPIC_INTERCEPTOR_CALL_PACKET_OUT+"'...");
		eventService.subscribe(SonarTopics.TOPIC_INTERCEPTOR_CALL_PACKET_OUT, new NetworkEventAction() {
			@Override
			public void handle(String event, byte[] payload) {
				PacketOutRequest packetOutRequest = PacketOutRequest.deserialize(payload);
				String destinationSwitchIp = IPUtils.convertInetToIPString(packetOutRequest.getDestination());
				LOGGER.info("Sending packetOut to "+destinationSwitchIp+".");
				if(deviceSockMap.containsKey(destinationSwitchIp)) {
					for(Socket deviceSock : deviceSockMap.get(destinationSwitchIp)) {
						if(deviceSock.isConnected() && !deviceSock.isClosed()) {
							try {
								OutputStream out = deviceSock.getOutputStream();
								out.write(packetOutRequest.getPacketOutBytes());
								out.flush();
								return;
							} catch (IOException e) {
								LOGGER.error("Error while trying to send a packout to "+destinationSwitchIp+".",e);
							}
						}
					}
					LOGGER.error("A connected socket was not found while trying to send a packout to "+destinationSwitchIp+".");
				}else {
					LOGGER.error("No socket was not found while trying to send a packout to "+destinationSwitchIp+".");
				}
			}
			
			@Override
			public void handle(String event, String json) {
				//do nothing... use raw version instead
			}
		});
	}
  
	@EventListener(ApplicationReadyEvent.class)
	public void run() {
    	ServerSocket serverSocket = null;
    	Socket socket = null;
        try {
            serverSocket = new ServerSocket(port);
            LOGGER.info("Listening for connections on port "+port+"...");
            while (true) {
                socket = serverSocket.accept();
                boolean anyControllerConnected = false;
                System.out.println("Instance:"+instance);
                List<Controller> controllerList = coreService.getControllersByInterceptor(instance);
                System.out.println("Controller:"+ (controllerList == null? "null" : ObjectUtils.toString(controllerList)));
                for(Controller controller : controllerList) {
                	String seed = controller.getSouth();
                	if(seed.contains(":")) {
                		anyControllerConnected = true;
                		String seedParts[] = seed.split(":",2);
                		new Thread(new Connection(new SonarSyncInterceptor(eventService),socket, seedParts[0], Integer.parseInt(seedParts[1]))).start();
                	}else {
                		LOGGER.error("Error while establishing connection to controller. Unable to determine seed.");
                		socket.close();
                	}
                }
                
                if(!anyControllerConnected) {
                	socket.close();
                }else {
                	registerDeviceSocket(socket);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
        	try {
        		if(socket != null) {
        			socket.close();
        		}
        		
        		if(serverSocket != null) {
        			serverSocket.close();
        		}
        	} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }

	private void registerDeviceSocket(Socket socket) {
		String ipSw = IPUtils.convertInetToIPString(socket.getInetAddress());
		LOGGER.info("Registering socket for device with IP:"+ipSw+".");
		if(deviceSockMap.containsKey(ipSw)) {
			List<Socket> closedSockets = new ArrayList<Socket>();
			for(Socket conSocket : deviceSockMap.get(ipSw)) {
				if(conSocket.isConnected() && !conSocket.isClosed()) {
					closedSockets.add(conSocket);
				}
			}
			deviceSockMap.get(ipSw).removeAll(closedSockets);
			deviceSockMap.get(ipSw).add(socket);
		}else {
			deviceSockMap.put(ipSw, new ArrayList<Socket>(Arrays.asList(socket)));
		}
		String pathSw = IPUtils.convertInetToIPString(socket.getLocalAddress())+":"+socket.getLocalPort();
		dataService.setData(SonarProperties.APPLICATION_CONTROLLER_INTERCEPTOR, SonarProperties.INSTANCE_SHARED, SonarProperties.GROUP_CI_PATH_TO_IP, pathSw, ipSw );
		eventService.publish(SonarTopics.TOPIC_INTERCEPTOR_NEW_CONNECTION, new ControllerInterceptionEvent(ipSw, pathSw));
	}
}