package br.ufu.facom.mehar.sonar.dhcp.service.impl;

import java.net.InetAddress;
import java.util.HashMap;
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

	private Map<String, InetAddress> macToIpMap = new HashMap<String, InetAddress>();

	private String poolCurrentip;

	public synchronized InetAddress getIP(String macAddress) {
		if (macToIpMap.containsKey(macAddress)) {
			return macToIpMap.get(macAddress);
		}

		InetAddress inetAddress = nextIP();
		
		poolCurrentip = IPUtils.convertInetToIPString(inetAddress);

		this.registerIP(macAddress, inetAddress);
		
		return inetAddress;
	}

	public synchronized void registerIP(String macAddress, InetAddress inetAddress) {
		macToIpMap.put(macAddress, inetAddress);
	}

	private synchronized InetAddress nextIP() {
		int[] currentip = IPUtils.convertIPStringToIntArray(poolCurrentip != null? poolCurrentip : poolFirstIp);
		int[] lastip = IPUtils.convertIPStringToIntArray(poolLastIp);
		
		if (currentip[3] < lastip[3]) {
			currentip[3]++;
			return IPUtils.convertIntArrayToInetAddress(currentip);
		} else {
			if (currentip[2] < lastip[2]) {
				currentip[2]++;
				currentip[3] = 0;
				return IPUtils.convertIntArrayToInetAddress(currentip);
			} else {
				if (currentip[1] < lastip[1]) {
					currentip[1]++;
					currentip[2] = 0;
					currentip[3] = 0;
					return IPUtils.convertIntArrayToInetAddress(currentip);
				} else {
					if (currentip[0] < lastip[0]) {
						currentip[0]++;
						currentip[1] = 0;
						currentip[2] = 0;
						currentip[3] = 0;
						return IPUtils.convertIntArrayToInetAddress(currentip);
					} else {
						throw new IpPoolOverflowException();
					}
				}
			}
		}
	}
}