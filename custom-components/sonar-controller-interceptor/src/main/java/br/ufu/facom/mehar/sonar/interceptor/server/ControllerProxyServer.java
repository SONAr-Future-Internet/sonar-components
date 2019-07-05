package br.ufu.facom.mehar.sonar.interceptor.server;

import java.util.List;

import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import br.ufu.facom.mehar.sonar.client.ndb.service.CoreDataService;
import br.ufu.facom.mehar.sonar.client.nem.action.NetworkEventAction;
import br.ufu.facom.mehar.sonar.client.nem.configuration.SonarTopics;
import br.ufu.facom.mehar.sonar.client.nem.service.EventService;
import br.ufu.facom.mehar.sonar.core.model.core.Controller;
import br.ufu.facom.mehar.sonar.core.util.IPUtils;
import br.ufu.facom.mehar.sonar.core.util.openflow.PacketOutRequest;
import br.ufu.facom.mehar.sonar.interceptor.filter.Interceptor;
import br.ufu.facom.mehar.sonar.interceptor.filter.SyncInterceptor;
import br.ufu.facom.mehar.sonar.interceptor.netty.OpenFlowChannelInitializer;
import br.ufu.facom.mehar.sonar.interceptor.netty.OpenFlowDecoder;
import br.ufu.facom.mehar.sonar.interceptor.openflow.OFMessageHolder;
import br.ufu.facom.mehar.sonar.interceptor.proxy.ChannelType;
import br.ufu.facom.mehar.sonar.interceptor.proxy.Connection;
import br.ufu.facom.mehar.sonar.interceptor.proxy.ConnectionManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

@Component
public class ControllerProxyServer {
	private static final Logger logger = LoggerFactory.getLogger(ControllerProxyServer.class);

	@Value("${interceptor.proxy.port:6600}")
	private Integer port;
	
	@Value("${sonar.ci.instance:192.168.0.1:6600}")
	private String instance;
	
	@Autowired
	private CoreDataService coreService;
	
	@Autowired
	private EventService eventService;
	
	/*
	 * Main sctructures
	 */
	private ConnectionManager connectionManager;
	private Interceptor interceptor;
	private OpenFlowDecoder openFlowDecoder; 
	/*
	 * Netty structures
	 */
	private EventLoopGroup serverGroup = new NioEventLoopGroup();
    private EventLoopGroup clientGroup = new NioEventLoopGroup();
	private ServerBootstrap serverBootstrap = new ServerBootstrap();
    private Bootstrap clientBootstrap = new Bootstrap();

	@EventListener(ApplicationReadyEvent.class)
	public void providePacketOut() {
		//Create Decoder
		openFlowDecoder = new OpenFlowDecoder();
		
		//Listen to requests
		logger.info("Listening for events of topic '"+SonarTopics.TOPIC_INTERCEPTOR_CALL_PACKET_OUT+"'...");
		eventService.subscribe(SonarTopics.TOPIC_INTERCEPTOR_CALL_PACKET_OUT, new NetworkEventAction() {
			@Override
			public void handle(String event, byte[] payload) {
				try {
					PacketOutRequest packetOutRequest = PacketOutRequest.deserialize(payload);
					if(packetOutRequest != null) {
						Connection connection = connectionManager.getConnection(packetOutRequest.getDestination());
						if(connection != null && connection.isReadyForInjectMessage()) {
							OFMessageHolder messageHolder = openFlowDecoder.decode(Unpooled.wrappedBuffer(packetOutRequest.getPacketOutBytes()));
							if(messageHolder != null) {
								//Send the PacketOut through connection
								connection.send(ChannelType.PROXY, ChannelType.SWITCH, messageHolder);
							}else {
								logger.error("Could not decode the OFMessageHolder of the PacketOut to "+ IPUtils.convertInetToIPString(packetOutRequest.getDestination()));
							}
						}else {
							logger.error("Unable to find the connection for sending a PacketOut to "+ IPUtils.convertInetToIPString(packetOutRequest.getDestination()));
						}
					}else {
						logger.error("Could not deserialize the PacketOutRequest received through event.");
					}
				} catch (OFParseError e) {
					logger.error("Error while parsing the OFMessagefor sending a PacketOut", e);
				} catch(Exception e) {
					logger.error("Error while parsing the OFMessagefor sending a PacketOut", e);
				}
			}
			
			@Override
			public void handle(String event, String json) {
				//do nothing... use raw version instead
			}
		});
	}
  
	@EventListener(ApplicationReadyEvent.class)
	public void start() {
        try {
        	//Querying controller info
        	List<Controller> controllerList = coreService.getControllersByInterceptor(instance);
        	Controller controllerIntercepted = controllerList.size() > 0? controllerList.iterator().next(): null;
        	if(controllerIntercepted != null) {
	            logger.info("Using controller "+controllerIntercepted.getStrategy()+" at "+controllerIntercepted.getSouth());
	            
	            //Create Interceptor
	            interceptor = new SyncInterceptor(eventService);
	            
	            //Create ConnectionManager
	            connectionManager = new ConnectionManager();
	            
	            //Set up Netty
	            serverBootstrap.group(serverGroup, clientGroup)
	            	.channel(NioServerSocketChannel.class)
	            	.childHandler(new OpenFlowChannelInitializer(connectionManager, interceptor, false));
	            clientBootstrap.group(clientGroup)
	            	.channel(NioSocketChannel.class)
	            	.handler(new OpenFlowChannelInitializer(connectionManager, interceptor,  true))
	            	.option(ChannelOption.TCP_NODELAY, true);
	            
	            //Configure ConnectionManager 
	            connectionManager.setBootstrap(clientBootstrap);
	            connectionManager.setControllerIP(controllerIntercepted.getSouth().split(":",2)[0]);
	            connectionManager.setControllerPort(Integer.parseInt(controllerIntercepted.getSouth().split(":",2)[1]));
	            
	            logger.info("Listening for connections on port "+port+"...");
	            serverBootstrap.bind(port);	            
        	}
        } catch (Exception e) {
            logger.error("Error while running interceptor!",e);
        }
    }
}