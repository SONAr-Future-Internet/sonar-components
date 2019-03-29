package br.ufu.facom.mehar.sonar.boot.manager;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BootManager {
	
	private Logger logger = Logger.getLogger(BootManager.class);

	@Autowired
	DHCPServer dhcpServer;
	
	@EventListener(ApplicationReadyEvent.class)
	public void run() {
		//Query CIM Registry and Run Minimal Containers
		logger.info("Starting SONAr components...");
		
		//Run DHCP Server
		dhcpServer.run();
		
		//Run LLDP Discovery
		logger.info("Starting LLDP discovery...");
		
		//Run DeviceConfigurator
		logger.info("Starting Device Configurator...");
		
	}
}
