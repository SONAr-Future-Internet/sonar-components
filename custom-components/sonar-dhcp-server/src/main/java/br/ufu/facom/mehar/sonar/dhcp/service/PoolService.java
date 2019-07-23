package br.ufu.facom.mehar.sonar.dhcp.service;

public interface PoolService {

	String getIP(String macAddress);
	
	void register(String macAddress, String ip);

	Boolean isAvailable(String ip);

	Boolean isRegistered(String macAddress, String ip);
}
