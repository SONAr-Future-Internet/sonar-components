package br.ufu.facom.mehar.sonar.interceptor.server;

import java.io.IOException;
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

import br.ufu.facom.mehar.sonar.core.util.IPUtils;
import br.ufu.facom.mehar.sonar.interceptor.configuration.CIConfiguration;
import br.ufu.facom.mehar.sonar.interceptor.manager.SonarSyncInterceptor;
import br.ufu.facom.mehar.sonar.interceptor.proxy.Connection;

@Component
public class ControllerProxyServer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerProxyServer.class);

	@Value("${interceptor.proxy.port:6600}")
	private Integer port;
	
	@Autowired
	private CIConfiguration configuration;
	
	private Map<String,List<Socket>> deviceSockMap = new HashMap<String, List<Socket>>();
  
	@EventListener(ApplicationReadyEvent.class)
	public void run() {
    	ServerSocket serverSocket = null;
    	Socket socket = null;
        try {
            serverSocket = new ServerSocket(port);
            LOGGER.info("Listening for connections on port "+port+"...");
            while (true) {
                socket = serverSocket.accept();
                for(String seed : configuration.getSDNSouthSeeds()) {
                	if(seed.contains(":")) {
                		String seedParts[] = seed.split(":",2);
                		registerDeviceSocket(socket);
                		new Thread(new Connection(new SonarSyncInterceptor() ,socket, seedParts[0], Integer.parseInt(seedParts[1]))).start();
                	}else {
                		LOGGER.error("Error while establishing connection to controller. Unable to determine seed.");
                		socket.close();
                	}
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
	}
}