package br.ufu.facom.mehar.sonar.dhcp.service;

import java.net.InetAddress;

public interface IPPoolService {

	InetAddress getIP(String macAddress);

}
