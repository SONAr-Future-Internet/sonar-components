package br.ufu.facom.mehar.sonar.boot.manager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.dhcp4java.DHCPConstants;
import org.dhcp4java.DHCPCoreServer;
import org.dhcp4java.DHCPOption;
import org.dhcp4java.DHCPPacket;
import org.dhcp4java.DHCPServerInitException;
import org.dhcp4java.DHCPServlet;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import br.ufu.facom.mehar.sonar.boot.model.Device;
import br.ufu.facom.mehar.sonar.boot.model.Device.SourceType;
import br.ufu.facom.mehar.sonar.boot.server.exception.IpPoolConversionException;
import br.ufu.facom.mehar.sonar.boot.server.exception.IpPoolOverflowException;
import br.ufu.facom.mehar.sonar.boot.service.DeviceService;

@Component
public class DHCPServer {
	Logger logger = Logger.getLogger(DHCPServer.class);


	@Value("${dhcp.bindAddress:0.0.0.0:67}")
	private String dhcpBindAddress; 
	
	@Value("${dhcp.serverThreads:1}")
	private String serverThreads;
	
	@Value("${dhcp.leaseTime:-1}")
	private String leaseTime;
	
	@Value("${dhcp.renewalTime:-1}")
	private String renewalTime;
	
	@Value("${dhcp.poolFirstIp:192.168.0.2}")
	private String poolFirstIp;
	
	@Value("${dhcp.poolLastIp:192.168.0.254}")
	private String poolLastIp;
	
	@Autowired
	DeviceService deviceService;
	
	public void run() {
		try {
			logger.info("Starting DHCP Server...");
			
			final InetAddress serverIdentifier = searchServerAddress();
			
			final Properties dhcpProperties = new Properties();
			dhcpProperties.setProperty(DHCPCoreServer.SERVER_ADDRESS, this.dhcpBindAddress);
			dhcpProperties.setProperty(DHCPCoreServer.SERVER_THREADS, this.serverThreads);
			
			final Integer leaseTime = Integer.parseInt(this.leaseTime);
			final Integer renewalTime = Integer.parseInt(this.renewalTime);
			
			final IPPool pool = this.new IPPool();
			
			logger.info("  - server: "+serverIdentifier+" bind: "+this.dhcpBindAddress+".");

			DHCPCoreServer server = DHCPCoreServer.initServer(new DHCPServlet() {
				@Override
				protected DHCPPacket doDiscover(DHCPPacket request) {
					logger.debug("doDiscover method invoked!");
					logger.debug(request.toString());
					
					String macAddress = request.getChaddrAsHex();
					InetAddress offeredInetAddress = pool.getIP(macAddress);
					
					logger.info("Discover from "+macAddress+" offered:"+offeredInetAddress);
					
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
					logger.debug("doRequest method invoked!");
					logger.debug(request.toString());
					
					String macAddress = request.getChaddrAsHex();
					InetAddress requestedAddress = request.getOptionAsInetAddr(DHCPConstants.DHO_DHCP_REQUESTED_ADDRESS);
					
					logger.info("Request "+requestedAddress+" from "+macAddress);
					
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
					
					deviceService.register(new Device(requestedAddress, macAddress, SourceType.DHCP));
					
					return response;
				}

				@Override
				protected DHCPPacket doInform(DHCPPacket request) {
					logger.info("doInform method no implemented yet!");
					logger.info(request.toString());
					return null;
				}

				@Override
				protected DHCPPacket doDecline(DHCPPacket request) {
					logger.info("doDecline method no implemented yet!");
					logger.info(request.toString());
					return null;
				}

				@Override
				protected DHCPPacket doRelease(DHCPPacket request) {
					logger.info("doRelease method no implemented yet!");
					logger.info(request.toString());
					return null;
				}
			}, dhcpProperties);
			new Thread(server).start();
		} catch (DHCPServerInitException e) {
			logger.fatal("DHCP Server init failure", e);
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
			logger.fatal("Error while detecting Server IP Address...", e);
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
							throw new IpPoolOverflowException();
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
				logger.fatal("Error converting String to InetAddress.",e);
				throw new IpPoolConversionException("Error converting String with Mask to InetAddress.", e);
			}
		}

		private InetAddress convertByteArrayToInetAddress(byte[] byteArray) {
			try {
				return InetAddress.getByAddress(byteArray);
			} catch (UnknownHostException e) {
				logger.fatal("Error converting ByteArray to InetAddress.",e);
				throw new IpPoolConversionException("Error converting ByteArray to InetAddress.", e);
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
