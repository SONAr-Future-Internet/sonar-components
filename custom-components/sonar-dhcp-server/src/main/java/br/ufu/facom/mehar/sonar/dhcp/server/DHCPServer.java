package br.ufu.facom.mehar.sonar.dhcp.server;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.stereotype.Component;

import br.ufu.facom.mehar.sonar.client.nem.configuration.SonarTopics;
import br.ufu.facom.mehar.sonar.client.nem.service.EventService;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;
import br.ufu.facom.mehar.sonar.core.util.IPUtils;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPConstants;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPCoreServer;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPOption;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPPacket;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPServerInitException;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPServlet;
import br.ufu.facom.mehar.sonar.dhcp.service.PoolService;

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

	@Value("${dhcp.subnetMask:255.255.255.0}")
	private String subnetMask;

	@Autowired
	@Qualifier("database")
	private PoolService poolService;
	
	@Autowired
	private EventService eventService;

	@EventListener(ApplicationReadyEvent.class)
	public void run() {
		try {
			logger.info("Starting DHCP Server...");
			
			final InterfaceAddress bindInterfaceAddress =  IPUtils.searchActiveInterfaceAddress();

			final InetAddress serverIP = bindInterfaceAddress.getAddress();
			final InetAddress broadcast = bindInterfaceAddress.getBroadcast();
			
			final Properties dhcpProperties = new Properties();
			dhcpProperties.setProperty(DHCPCoreServer.SERVER_ADDRESS, this.dhcpBindAddress);
			dhcpProperties.setProperty(DHCPCoreServer.SERVER_THREADS, this.serverThreads);

			final Integer leaseTime = Integer.parseInt(this.leaseTime);
			final Integer renewalTime = Integer.parseInt(this.renewalTime);

			logger.info("  - server: " + serverIP + " bind: " + this.dhcpBindAddress + ".");

			DHCPCoreServer server = DHCPCoreServer.initServer(new DHCPServlet() {
				@Override
				protected DHCPPacket doDiscover(DHCPPacket request) {
					logger.debug("doDiscover method invoked!");
					logger.debug(request.toString());

					String macAddress = request.getChaddrAsHex();
					InetAddress offeredInetAddress = IPUtils.convertIPStringToInetAddress(poolService.getIP(macAddress));

					logger.info("Discover from " + macAddress + " offered:" + offeredInetAddress);

					DHCPPacket response = request.clone();

					response.setDHCPMessageType(DHCPConstants.BOOTREPLY);
					response.setOp(DHCPConstants.DHCPOFFER);
					response.setYiaddr(offeredInetAddress);
					response.setSiaddr(serverIP);
					response.setSecs((short) (request.getSecs() + 1));

					response.setAddress(broadcast);

					Collection<DHCPOption> options = new ArrayList<DHCPOption>();
					options.add(
							DHCPOption.newOptionAsByte(DHCPConstants.DHO_DHCP_MESSAGE_TYPE, DHCPConstants.DHCPOFFER));
					options.add(DHCPOption.newOptionAsInetAddress(DHCPConstants.DHO_DHCP_SERVER_IDENTIFIER, serverIP));
					options.add(DHCPOption.newOptionAsInt(DHCPConstants.DHO_DHCP_LEASE_TIME, leaseTime));
					options.add(DHCPOption.newOptionAsInt(DHCPConstants.DHO_DHCP_RENEWAL_TIME, renewalTime));
					options.add(DHCPOption.newOptionAsInetAddress(DHCPConstants.DHO_SUBNET_MASK,
							IPUtils.convertIPStringToInetAddress(subnetMask)));

					response.setOptions(options);

					return response;
				}

				@Override
				protected DHCPPacket doRequest(DHCPPacket request) {
					logger.debug("doRequest method invoked!");
					logger.debug(request.toString());

					String macAddress = request.getChaddrAsHex();
					InetAddress requestedAddress = request
							.getOptionAsInetAddr(DHCPConstants.DHO_DHCP_REQUESTED_ADDRESS);

					logger.info("Request " + requestedAddress + " from " + macAddress);

					DHCPPacket response = request.clone();

					response.setDHCPMessageType(DHCPConstants.BOOTREPLY);
					response.setOp(DHCPConstants.DHCPACK);
					response.setYiaddr(requestedAddress);
					response.setSiaddr(serverIP);
					response.setSecs((short) (request.getSecs() + 1));

					response.setAddress(broadcast);

					Collection<DHCPOption> options = new ArrayList<DHCPOption>();
					options.add(DHCPOption.newOptionAsByte(DHCPConstants.DHO_DHCP_MESSAGE_TYPE, DHCPConstants.DHCPACK));
					options.add(DHCPOption.newOptionAsInetAddress(DHCPConstants.DHO_DHCP_SERVER_IDENTIFIER, serverIP));
					options.add(DHCPOption.newOptionAsInt(DHCPConstants.DHO_DHCP_LEASE_TIME, leaseTime));
					options.add(DHCPOption.newOptionAsInt(DHCPConstants.DHO_DHCP_RENEWAL_TIME, renewalTime));
					options.add(DHCPOption.newOptionAsInetAddress(DHCPConstants.DHO_SUBNET_MASK,
							IPUtils.convertIPStringToInetAddress(subnetMask)));

					response.setOptions(options);
					
					Port port = new Port();
					port.setMacAddress(macAddress);
					port.setIpAddress(requestedAddress.getHostAddress());

					eventService.publish(SonarTopics.TOPIC_TOPOLOGY_PORT_IP_ASSIGNED, port);

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
}
