package br.ufu.facom.mehar.sonar.dhcp.service.impl;

import java.net.InetAddress;
import java.util.Date;

import org.jboss.logging.Logger;
import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.dhcp.service.DHCPEventService;

@Service("log")
public class LogDHCPEventService implements DHCPEventService {

	Logger logger = Logger.getLogger(LogDHCPEventService.class);

	@Override
	public void publishNewIpConfiguredEvent(InetAddress requestedAddress, String macAddress, InetAddress serverIP,
			Date date) {
		logger.info("NEW_IP_CONFIGURED_EVENT : address: " + requestedAddress + " on macAddress:" + macAddress
				+ ". DHCP:" + serverIP + " Time:" + date);

	}

}
