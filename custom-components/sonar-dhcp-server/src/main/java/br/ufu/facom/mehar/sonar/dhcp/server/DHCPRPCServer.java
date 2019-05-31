package br.ufu.facom.mehar.sonar.dhcp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import br.ufu.facom.mehar.sonar.client.nem.action.NetworkEventAction;
import br.ufu.facom.mehar.sonar.client.nem.configuration.SonarTopics;
import br.ufu.facom.mehar.sonar.client.nem.service.EventService;

@Component
public class DHCPRPCServer {
	Logger logger = LoggerFactory.getLogger(DHCPRPCServer.class);
	
	@Autowired
	private SONArDHCPEngine dhcpServlet;
	
	@Autowired
	private EventService eventService;

	@EventListener(ApplicationReadyEvent.class)
	public void run() {
		logger.info("Starting DHCP RPC Server...");
		eventService.subscribe(SonarTopics.TOPIC_DHCP_MESSAGE_INCOMING, new NetworkEventAction() {
			@Override
			public void handle(String event, String json) {
				logger.info("Dealing with DHCP Request Event...");
			}
		});	
	}
}
