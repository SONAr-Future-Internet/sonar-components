package br.ufu.facom.mehar.sonar.dhcp.server;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import br.ufu.facom.mehar.sonar.dhcp.api.DHCPCoreServer;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPServerInitException;
import br.ufu.facom.mehar.sonar.dhcp.engine.ConventionalDHCPEngine;
import br.ufu.facom.mehar.sonar.dhcp.engine.DHCPEngine;

@Component
public class DHCPServer {
	Logger logger = LoggerFactory.getLogger(DHCPServer.class);
	
	@Value("${sonar.server.local.ip.address:192.168.0.1}")
	private String serverLocalIpAddress;

	@Value("${dhcp.bindAddress:0.0.0.0:67}")
	private String dhcpBindAddress;

	@Value("${dhcp.serverThreads:1}")
	private String serverThreads;
	
	@Autowired
	private ConventionalDHCPEngine dhcpServlet;

	@EventListener(ApplicationReadyEvent.class)
	public void run() {
		try {
			logger.info("Starting DHCP Server...");
			
			final Properties dhcpProperties = new Properties();
			dhcpProperties.setProperty(DHCPCoreServer.SERVER_ADDRESS, this.dhcpBindAddress);
			dhcpProperties.setProperty(DHCPCoreServer.SERVER_THREADS, this.serverThreads);

			logger.info("  - server: " + serverLocalIpAddress + " bind: " + this.dhcpBindAddress + ".");

			DHCPCoreServer server = DHCPCoreServer.initServer(dhcpServlet, dhcpProperties);
			new Thread(server).start();
		} catch (DHCPServerInitException e) {
			logger.error("DHCP Server init failure", e);
		}
	}
}
