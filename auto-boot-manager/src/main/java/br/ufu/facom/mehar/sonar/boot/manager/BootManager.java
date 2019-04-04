package br.ufu.facom.mehar.sonar.boot.manager;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import br.ufu.facom.mehar.sonar.boot.server.exception.InvalidEndpointException;
import br.ufu.facom.mehar.sonar.client.cim.Component;
import br.ufu.facom.mehar.sonar.client.cim.service.ComponentService;
import br.ufu.facom.mehar.sonar.core.model.container.Container;
import br.ufu.facom.mehar.sonar.core.model.container.ContainerStatus;

@org.springframework.stereotype.Component
public class BootManager {
	
	private Logger logger = Logger.getLogger(BootManager.class);
	
	@Autowired
	private ComponentService componentService;
	
	@Value("${cim.manager.ip:localhost}")
	private String CIM_IP;
	
	@Value("${cim.manager.port:8080}")
	private String CIM_PORT;
	
	@Value("${boot.manager.dhcp.enabled:true}")
	private Boolean DHCP_ENABLED;
	
	private Map<Component, Container> containerMap;
	
	@EventListener(ApplicationReadyEvent.class)
	public void run() {
		logger.info("Booting network...");
		//Query already created components
		containerMap = componentService.get(CIM_IP);

		Container nddb = checkAndRunComponent(Component.DistributedNetworkDatabase);
		if(nddb == null) {
			logger.fatal("Unable to boot network without 'DistributedNetworkDatabase'.");
			return;
		}
		
		Container nem = checkAndRunComponent(Component.NetworkEventManager);
		if(nem == null) {
			logger.fatal("Unable to boot network without 'NetworkEventManager'.");
			return;
		}
		
		Properties properties = new Properties();
		String nddbEndpoint = findEndPoint(Component.DistributedNetworkDatabase, nddb, "main");
		String nemEndpoint = findEndPoint(Component.NetworkEventManager, nem, "main");
		properties.setProperty("NDDB_SEEDS", nddbEndpoint);
		properties.setProperty("NEM_SEEDS", nemEndpoint);
		
		//Verify and Run DHCP
		if(DHCP_ENABLED) {
			checkAndRunComponent(Component.DHCPServer, properties);
		}
		
		//checkAndRunComponent(Component.TopologySelfCollectorEntity, properties);
	}


	private String findEndPoint(Component component, Container container, String accessPort) {
		String port = container.getAccessPort().get(accessPort);
		if(port != null) {
			if(container.getServer() != null && !container.getServer().isEmpty() && !container.getServer().equals("local")) {
				return container.getServer()+":"+port;
			}else {
				throw new InvalidEndpointException("Invalid endpoint "+accessPort+" of '"+component+"'. Server is invalid! server = "+container.getServer()+".");
			}
		}else {
			throw new InvalidEndpointException("Invalid endpoint "+accessPort+" of '"+component+"'. Port not specified! ports: "+container.getAccessPort()+".");
		}
	}

	private Container checkAndRunComponent(Component component, Properties properties) {
		try {
			logger.info("Starting '"+component+"'...");
			Container container = containerMap.get(component);
			if(container != null) {
				logger.info("| '"+component+"' is already created! Removing container...");
				container = componentService.deleteComponent(CIM_IP, component);
				if(ContainerStatus.REMOVED_STATE.equals(container.getStatus())) {
					logger.info("| '"+component+"' removed! Recreating and running container...");
				}else {
					logger.info("| '"+component+"' not removed! Status: "+container.getStatus()+". Ignoring Error.");
					return null;
				}
			}
			
			container = componentService.runComponent(CIM_IP, component, properties);
			logger.info("| '"+component+"' is running.");
			logger.debug( ToStringBuilder.reflectionToString(container) );
			containerMap.put(component, container);
			return container;
		}catch(Exception e) {
			logger.error("| Error running '"+component+"' component. Ignoring error.", e);
			return null;
		}
	}

	private Container checkAndRunComponent(Component component) {
		try {
			logger.info("Starting '"+component+"'...");
			Container container = containerMap.get(component);
			if(container != null) {
				if(ContainerStatus.RUNNING_STATE.equals(container.getStatus())) {
					logger.info("| '"+component+"' is already created and running!");
					logger.debug( ToStringBuilder.reflectionToString(container) );
					return container;
				}{
					logger.info("| '"+component+"' is already created but not running. Running container...");
					container = componentService.runComponent(CIM_IP, component);
					logger.info("| '"+component+"' is running!");
					logger.debug( ToStringBuilder.reflectionToString(container) );
					containerMap.put(component, container);
					return container;
				}
			}else {
				container = componentService.runComponent(CIM_IP, component);
				logger.info("| '"+component+"' is running.");
				logger.debug( ToStringBuilder.reflectionToString(container) );
				containerMap.put(component, container);
				return container;
			}
		}catch(Exception e) {
			logger.error("| Error running '"+component+"' component. Ignoring error.", e);
			return null;
		}
	}
}
