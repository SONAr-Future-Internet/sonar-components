package br.ufu.facom.mehar.sonar.dhcp.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.core.util.IPUtils;
import br.ufu.facom.mehar.sonar.dhcp.exception.IpPoolOverflowException;
import br.ufu.facom.mehar.sonar.dhcp.service.PoolService;

@Service
public class MemoryPoolService implements PoolService {

	@Value("${dhcp.poolFirstIp:192.168.0.2}")
	private String poolFirstIp;

	@Value("${dhcp.poolLastIp:192.168.0.254}")
	private String poolLastIp;
	
	private Map<String, String> macToIp = new HashMap<String, String>();
	private List<String> busyIPs = new ArrayList<String>();

	public synchronized String getIP(String macAddress) {
		if (macToIp.containsKey(macAddress)) {
			return macToIp.get(macAddress);
		}
		
		
		String currentIp = poolFirstIp;
		while(!currentIp.equals(poolLastIp)) {
			if(!busyIPs.contains(currentIp)) {
				macToIp.put(macAddress, currentIp);
				busyIPs.add(currentIp);
				return currentIp;
			}else {
				currentIp = IPUtils.nextIP(currentIp);
			}
		}

		//Overflow
		throw new IpPoolOverflowException("Max IP of Pool '"+poolLastIp+"' was reached!");
	}

	public String getPoolFirstIp() {
		return poolFirstIp;
	}

	public void setPoolFirstIp(String poolFirstIp) {
		this.poolFirstIp = poolFirstIp;
	}

	public String getPoolLastIp() {
		return poolLastIp;
	}

	public void setPoolLastIp(String poolLastIp) {
		this.poolLastIp = poolLastIp;
	}

	public Map<String, String> getMacToIp() {
		return macToIp;
	}

	public void setMacToIp(Map<String, String> macToIp) {
		this.macToIp = macToIp;
	}

	public List<String> getBusyIPs() {
		return busyIPs;
	}

	public void setBusyIPs(List<String> busyIPs) {
		this.busyIPs = busyIPs;
	}
}