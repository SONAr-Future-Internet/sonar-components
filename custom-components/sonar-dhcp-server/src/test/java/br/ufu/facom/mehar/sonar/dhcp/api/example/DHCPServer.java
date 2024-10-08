package br.ufu.facom.mehar.sonar.dhcp.api.example;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import br.ufu.facom.mehar.sonar.dhcp.api.DHCPConstants;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPCoreServer;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPOption;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPPacket;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPServerInitException;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPServlet;

public class DHCPServer {
	private String dhcpBindAddress = "0.0.0.0:67"; 
	private String serverThreads = "1";
	private String leaseTime = "-1";
	private String renewalTime = "-1";
	private String poolFirstIp = "192.168.0.2";
	private String poolLastIp = "192.168.0.254";
	
	
	public static void main(String[] args) {
		new DHCPServer().run();
	}
	
	public void run() {
		try {
			System.out.println("Starting DHCP Server...");
			
			final InetAddress serverIdentifier = searchServerAddress();
			
			final Properties dhcpProperties = new Properties();
			dhcpProperties.setProperty(DHCPCoreServer.SERVER_ADDRESS, this.dhcpBindAddress);
			dhcpProperties.setProperty(DHCPCoreServer.SERVER_THREADS, this.serverThreads);
			
			final Integer leaseTime = Integer.parseInt(this.leaseTime);
			final Integer renewalTime = Integer.parseInt(this.renewalTime);
			
			final IPPool pool = this.new IPPool();

			DHCPCoreServer server = DHCPCoreServer.initServer(new DHCPServlet() {
				@Override
				protected DHCPPacket doDiscover(DHCPPacket request) {
					System.out.println("doDiscover method invoked!");
					System.out.println(request.toString());
					
					String macAddress = request.getChaddrAsHex();
					InetAddress offeredInetAddress = pool.getIP(macAddress);
					
					DHCPPacket response = request.clone();
					
					response.setDHCPMessageType(DHCPConstants.BOOTREPLY);
					response.setOp(DHCPConstants.DHCPOFFER);
					response.setYiaddr(offeredInetAddress);
					response.setSiaddr(serverIdentifier);
					response.setSecs((short) (request.getSecs()+1));
					
					response.setAddress(pool.convertIpStringToInetAddress(pool.getBroadcast()));
					
					Collection<DHCPOption> options = new ArrayList<DHCPOption>();
					options.add( DHCPOption.newOptionAsByte(DHCPConstants.DHO_DHCP_MESSAGE_TYPE,  DHCPConstants.DHCPOFFER) );
					options.add( DHCPOption.newOptionAsInetAddress(DHCPConstants.DHO_DHCP_SERVER_IDENTIFIER,  serverIdentifier) );
					options.add( DHCPOption.newOptionAsInt(DHCPConstants.DHO_DHCP_LEASE_TIME,  leaseTime) );
					options.add( DHCPOption.newOptionAsInt(DHCPConstants.DHO_DHCP_RENEWAL_TIME,  renewalTime) );
					options.add( DHCPOption.newOptionAsInetAddress(DHCPConstants.DHO_SUBNET_MASK,  pool.getMask()) );
					
					response.setOptions(options);
					
					
					return response;
				}

				@Override
				protected DHCPPacket doRequest(DHCPPacket request) {
					System.out.println("doRequest method invoked!");
					System.out.println(request.toString());
					
					String macAddress = request.getChaddrAsHex();
					InetAddress requestedAddress = request.getOptionAsInetAddr(DHCPConstants.DHO_DHCP_REQUESTED_ADDRESS);
					
					DHCPPacket response = request.clone();

					response.setDHCPMessageType(DHCPConstants.BOOTREPLY);
					response.setOp(DHCPConstants.DHCPACK);
					response.setYiaddr(requestedAddress);
					response.setSiaddr(serverIdentifier);
					response.setSecs((short) (request.getSecs()+1));
					
					response.setAddress(pool.convertIpStringToInetAddress(pool.getBroadcast()));
					
					Collection<DHCPOption> options = new ArrayList<DHCPOption>();
					options.add( DHCPOption.newOptionAsByte(DHCPConstants.DHO_DHCP_MESSAGE_TYPE,  DHCPConstants.DHCPACK) );
					options.add( DHCPOption.newOptionAsInetAddress(DHCPConstants.DHO_DHCP_SERVER_IDENTIFIER,  serverIdentifier) );
					options.add( DHCPOption.newOptionAsInt(DHCPConstants.DHO_DHCP_LEASE_TIME,  leaseTime) );
					options.add( DHCPOption.newOptionAsInt(DHCPConstants.DHO_DHCP_RENEWAL_TIME,  renewalTime) );
					options.add( DHCPOption.newOptionAsInetAddress(DHCPConstants.DHO_SUBNET_MASK,  pool.getMask()) );
					
					response.setOptions(options);
					
					return response;
				}

				@Override
				protected DHCPPacket doInform(DHCPPacket request) {
					System.out.println("doInform method no implemented yet!");
					System.out.println(request.toString());
					return null;
				}

				@Override
				protected DHCPPacket doDecline(DHCPPacket request) {
					System.out.println("doDecline method no implemented yet!");
					System.out.println(request.toString());
					return null;
				}

				@Override
				protected DHCPPacket doRelease(DHCPPacket request) {
					System.out.println("doRelease method no implemented yet!");
					System.out.println(request.toString());
					return null;
				}
			}, dhcpProperties);
			new Thread(server).start();
		} catch (DHCPServerInitException e) {
			System.err.println("DHCP Server init failure");
		}
	}

	private InetAddress searchServerAddress() {
		Enumeration<NetworkInterface> enumInterface;
		try {
			enumInterface = NetworkInterface.getNetworkInterfaces();
			
			while(enumInterface.hasMoreElements()) {
				NetworkInterface netInf = enumInterface.nextElement();
				
				if(!netInf.isLoopback() && !netInf.isVirtual() && netInf.isUp()) {
					Enumeration<InetAddress> enumAddress = netInf.getInetAddresses();
					while(enumAddress.hasMoreElements()) {
						InetAddress addressInf = enumAddress.nextElement();
						
						if(addressInf instanceof Inet4Address && !addressInf.isLoopbackAddress()) {
							return addressInf;
						}
					}
				}
			}
		} catch (SocketException e) {
			System.err.println("Error while detecting Server IP Address...");
		}
		return null;
	}
	
	class IPPool {
		private Map<String, InetAddress> macToIpMap = new HashMap<String, InetAddress>();
		
		private int[] currentip;
		private int[] firstip;
		private int[] lastip;
		
		public IPPool() {
			this.currentip = this.convertIpStringToIntArray(poolFirstIp);
			this.firstip = this.convertIpStringToIntArray(poolFirstIp);
			this.lastip = this.convertIpStringToIntArray(poolLastIp);
		}
		
		public synchronized InetAddress getIP(String macAddress) {
			if(macToIpMap.containsKey(macAddress)) {
				return macToIpMap.get(macAddress);
			}
			
			InetAddress inetAddress = nextIP();
			this.registerIP(macAddress, inetAddress);
			return inetAddress;
		}
		
		public synchronized void registerIP(String macAddress, InetAddress inetAddress) {
			macToIpMap.put(macAddress, inetAddress);
		}

		private synchronized InetAddress nextIP() {
			if(currentip[3] < lastip[3]) {
				currentip[3]++;
				return convertIntArrayToInetAddress(currentip);
			}else {
				if(currentip[2] < lastip[2]) {
					currentip[2]++;
					currentip[3] = 0;
					return convertIntArrayToInetAddress(currentip);
				}else {
					if(currentip[1] < lastip[1]) {
						currentip[1]++;
						currentip[2] = 0;
						currentip[3] = 0;
						return convertIntArrayToInetAddress(currentip);
					}else {
						if(currentip[0] < lastip[0]) {
							currentip[0]++;
							currentip[1] = 0;
							currentip[2] = 0;
							currentip[3] = 0;
							return convertIntArrayToInetAddress(currentip);
						}else {
							throw new RuntimeException();
						}
					}
				}
			}
		}

		private String calculateMask() {
			String mask = "";
			for(int i=0; i<firstip.length; i++) {
				if(!mask.isEmpty()) {
					mask += ".";
				}
				
				if(firstip[i] == lastip[i]) {
					mask += "255";
				}else {
					mask += "0";
				}
			}
			return mask;
		}
		
		public String getBroadcast() {
			String broadcast = "";
			for(int i=0; i<firstip.length; i++) {
				if(!broadcast.isEmpty()) {
					broadcast += ".";
				}
				
				if(firstip[i] == lastip[i]) {
					broadcast += firstip[i];
				}else {
					broadcast += 255;
				}
			}
			return broadcast;
		}
		
		private int[] convertIpStringToIntArray(String ipString) {
			int[] array = new int[4];
			String[] parts = ipString.split("\\.",4);
			
			for(int i = 0; i < parts.length; i++ ) {
				array[i] = Integer.parseInt(parts[i]);
			}
			
			return array;
		}
		
		private InetAddress convertIntArrayToInetAddress(int[] arrayInt) {
			String ipString = arrayInt[0]+"."+arrayInt[1]+"."+arrayInt[2]+"."+arrayInt[3];
			return this.convertIpStringToInetAddress(ipString);
		}

		
		private InetAddress convertIpStringToInetAddress(String ipString) {
			try {
				return InetAddress.getByName(ipString);
			} catch (UnknownHostException e) {
				System.err.println("Error converting String to InetAddress.");
				throw new RuntimeException("Error converting String with Mask to InetAddress. Value:"+ipString, e);
			}
		}

		private InetAddress convertByteArrayToInetAddress(byte[] byteArray) {
			try {
				return InetAddress.getByAddress(byteArray);
			} catch (UnknownHostException e) {
				System.err.println("Error converting ByteArray to InetAddress.");
				throw new RuntimeException("Error converting ByteArray to InetAddress. Value:"+byteArray, e);
			}
		}

		private byte[] convertIpStringToByteArray(String ipString) {
			return this.convertIpStringToInetAddress(ipString).getAddress();
		}
		
		public InetAddress getMask() {
			return this.convertIpStringToInetAddress(this.calculateMask());
		}
	}
}
