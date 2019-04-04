package br.ufu.facom.mehar.sonar.dhcp.service;

import java.net.InetAddress;

public interface PoolService {

	InetAddress getIP(String macAddress);

}
