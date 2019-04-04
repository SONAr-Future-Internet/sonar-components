package br.ufu.facom.mehar.sonar.dhcp.service;

import java.net.InetAddress;
import java.util.Date;

public interface EventService {

	void publishNewIpConfiguredEvent(InetAddress requestedAddress, String macAddress, InetAddress serverIP, Date date);

}
