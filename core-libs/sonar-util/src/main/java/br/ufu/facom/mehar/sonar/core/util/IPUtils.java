package br.ufu.facom.mehar.sonar.core.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.apache.commons.net.util.SubnetUtils;

import br.ufu.facom.mehar.sonar.core.util.exception.IPConversionException;
import br.ufu.facom.mehar.sonar.core.util.exception.IPOverflowException;
import br.ufu.facom.mehar.sonar.core.util.exception.SonarUtilException;

public class IPUtils {
	public static InetAddress convertIntArrayToInetAddress(int[] arrayInt) {
		String ipString = arrayInt[0]+"."+arrayInt[1]+"."+arrayInt[2]+"."+arrayInt[3];
		return IPUtils.convertIPStringToInetAddress(ipString);
	}
	
	public static byte[] convertIPStringToByteArray(String ipString) {
		return IPUtils.convertIPStringToInetAddress(ipString).getAddress();
	}

	public static InetAddress convertIPStringToInetAddress(String ipString) {
		try {
			return InetAddress.getByName(ipString);
		} catch (UnknownHostException e) {
			throw new IPConversionException("Error converting String with Mask to InetAddress.", e);
		}
	}

	public static InetAddress convertByteArrayToInetAddress(byte[] byteArray) {
		try {
			return InetAddress.getByAddress(byteArray);
		} catch (UnknownHostException e) {
			throw new IPConversionException("Error converting ByteArray to InetAddress.", e);
		}
	}

	public static String nextIP(String currentIPString, String maxIPString) {
		String newIPString = IPUtils.nextIP(currentIPString);
		if(IPUtils.compare(newIPString, maxIPString) > 0) {
			throw new IPOverflowException();
		}
		return newIPString;
	}
	
	public static int compare(String firstIPString, String secondIPString) {
		int[] firstIP = IPUtils.convertIPStringToIntArray(firstIPString);
		int[] secondIP = IPUtils.convertIPStringToIntArray(secondIPString);
		
		for(int i=0; i<4; i++) {
			if(firstIP[i] < secondIP[i]) {
				return -1;
			}else {
				if(firstIP[i] > secondIP[i]) {
					return 1;
				}
			}
		}
		return 0;
	}

	public static String nextIP(String currentIPString) {	
		int[] currentip = IPUtils.convertIPStringToIntArray(currentIPString);
		
		if(currentip[3] < 255) {
			currentip[3]++;
			return IPUtils.convertIntArrayToIPString(currentip);
		}else {
			if(currentip[2] < 255) {
				currentip[2]++;
				currentip[3] = 0;
				return IPUtils.convertIntArrayToIPString(currentip);
			}else {
				if(currentip[1] < 255) {
					currentip[1]++;
					currentip[2] = 0;
					currentip[3] = 0;
					return IPUtils.convertIntArrayToIPString(currentip);
				}else {
					if(currentip[0] < 255) {
						currentip[0]++;
						currentip[1] = 0;
						currentip[2] = 0;
						currentip[3] = 0;
						return IPUtils.convertIntArrayToIPString(currentip);
					}else {
						throw new IPOverflowException();
					}
				}
			}
		}
	}

	private static String convertIntArrayToIPString(int[] currentip) {
		return currentip[0] + "."+currentip[1]+"."+currentip[2]+"."+currentip[3]; 
	}

	public static int[] convertIPStringToIntArray(String currentIPString) {
		int ip[] = new int[4];
		String parts[] = currentIPString.split("\\.",4);
		
		for(int i=0; i<4; i++) {
			ip[i] = Integer.parseInt(parts[i]);
		}
		
		return ip;
	}

	public static String calculateNetworkAddress(String ip, String networkMask) {
		SubnetUtils utils = new SubnetUtils(ip, networkMask);
		return utils.getInfo().getAddress();
	}
	
	public static Boolean isInNetwork(String ip, String address, String networkMask) {
		SubnetUtils utils = new SubnetUtils(address, networkMask);
		return utils.getInfo().isInRange(ip) || utils.getInfo().getBroadcastAddress().equals(ip) ||  utils.getInfo().getAddress().equals(ip);
	}
	
	public static Boolean isInRange(String ip, String firstIp, String lastIp) {
		return (compare(ip, firstIp) >= 0 && compare(ip, lastIp) <= 0);
	}

	
	public static InterfaceAddress searchActiveInterfaceAddress() {
		Enumeration<NetworkInterface> enumInterface;
		try {
			enumInterface = NetworkInterface.getNetworkInterfaces();

			while (enumInterface.hasMoreElements()) {
				NetworkInterface netInf = enumInterface.nextElement();

				if (!netInf.isLoopback() && !netInf.isVirtual() && netInf.isUp() && !netInf.getName().startsWith("docker")) {
					for (InterfaceAddress interfaceAddress : netInf.getInterfaceAddresses()) {
						InetAddress addressInf = interfaceAddress.getAddress();
						if (addressInf instanceof Inet4Address && !addressInf.isLoopbackAddress()) {
							return interfaceAddress;
						}
					}
				}
			}
		} catch (SocketException e) {
			throw new SonarUtilException("Error while detecting Server IP Address.", e);
		}
		return null;
	}
	
	public static InterfaceAddress searchDockerInterfaceAddress() {
		Enumeration<NetworkInterface> enumInterface;
		try {
			enumInterface = NetworkInterface.getNetworkInterfaces();

			while (enumInterface.hasMoreElements()) {
				NetworkInterface netInf = enumInterface.nextElement();

				if (!netInf.isLoopback() && !netInf.isVirtual() && netInf.isUp() && netInf.getName().startsWith("docker")) {
					for (InterfaceAddress interfaceAddress : netInf.getInterfaceAddresses()) {
						InetAddress addressInf = interfaceAddress.getAddress();
						if (addressInf instanceof Inet4Address && !addressInf.isLoopbackAddress()) {
							return interfaceAddress;
						}
					}
				}
			}
		} catch (SocketException e) {
			throw new SonarUtilException("Error while detecting Server IP Address.", e);
		}
		return null;
	}

	public static String convertInetToIPString(InetAddress inetAddress) {
		return inetAddress.getHostAddress();
	}

	public static String normalizeMAC(String macAddress) {
		macAddress = macAddress.toLowerCase();
		if(!macAddress.contains(":")) {
			String newMacAddress = "";
			for(int i=0; i< macAddress.length(); i=i+2) {
				if(!newMacAddress.isEmpty()) {
					newMacAddress += ":";
				}
				newMacAddress += macAddress.substring(i, i+2);
			}
			return newMacAddress;
		}
		return macAddress;
	}
}
