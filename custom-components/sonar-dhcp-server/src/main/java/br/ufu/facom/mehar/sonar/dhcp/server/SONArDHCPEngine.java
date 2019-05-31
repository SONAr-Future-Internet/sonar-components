package br.ufu.facom.mehar.sonar.dhcp.server;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import br.ufu.facom.mehar.sonar.client.nem.configuration.SonarTopics;
import br.ufu.facom.mehar.sonar.client.nem.service.EventService;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;
import br.ufu.facom.mehar.sonar.core.util.IPUtils;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPConstants;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPOption;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPPacket;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPServlet;
import br.ufu.facom.mehar.sonar.dhcp.service.PoolService;

@Component
class SONArDHCPEngine extends DHCPServlet {
	@Value("${sonar.server.local.ip.address:192.168.0.1}")
	private String serverLocalIpAddress;

	@Value("${sonar.server.local.ip.broadcast:192.168.0.255}")
	private String serverLocalIpBroadcast;

	@Value("${sonar.server.local.ip.mask:255.255.255.0}")
	private String serverLocalIpMask;

	@Value("${dhcp.bindAddress:0.0.0.0:67}")
	private String dhcpBindAddress;

	@Value("${dhcp.serverThreads:1}")
	private String serverThreads;

	@Value("${dhcp.leaseTime:-1}")
	private Integer leaseTime;

	@Value("${dhcp.renewalTime:-1}")
	private Integer renewalTime;

	@Autowired
	@Qualifier("database")
	private PoolService poolService;

	@Autowired
	private EventService eventService;

	Logger logger = LoggerFactory.getLogger(SONArDHCPEngine.class);

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
		response.setSiaddr(IPUtils.convertIPStringToInetAddress(serverLocalIpAddress));
		response.setSecs((short) (request.getSecs() + 1));

		response.setAddress(IPUtils.convertIPStringToInetAddress(serverLocalIpBroadcast));

		Collection<DHCPOption> options = new ArrayList<DHCPOption>();
		options.add(DHCPOption.newOptionAsByte(DHCPConstants.DHO_DHCP_MESSAGE_TYPE, DHCPConstants.DHCPOFFER));
		options.add(DHCPOption.newOptionAsInetAddress(DHCPConstants.DHO_DHCP_SERVER_IDENTIFIER,
				IPUtils.convertIPStringToInetAddress(serverLocalIpAddress)));
		options.add(DHCPOption.newOptionAsInt(DHCPConstants.DHO_DHCP_LEASE_TIME, leaseTime));
		options.add(DHCPOption.newOptionAsInt(DHCPConstants.DHO_DHCP_RENEWAL_TIME, renewalTime));
		options.add(DHCPOption.newOptionAsInetAddress(DHCPConstants.DHO_SUBNET_MASK,
				IPUtils.convertIPStringToInetAddress(serverLocalIpMask)));

		response.setOptions(options);

		return response;
	}

	@Override
	protected DHCPPacket doRequest(DHCPPacket request) {
		logger.debug("doRequest method invoked!");
		logger.debug(request.toString());

		String macAddress = request.getChaddrAsHex();
		InetAddress requestedAddress = request.getOptionAsInetAddr(DHCPConstants.DHO_DHCP_REQUESTED_ADDRESS);

		logger.info("Request " + requestedAddress + " from " + macAddress);

		DHCPPacket response = request.clone();

		response.setDHCPMessageType(DHCPConstants.BOOTREPLY);
		response.setOp(DHCPConstants.DHCPACK);
		response.setYiaddr(requestedAddress);
		response.setSiaddr(IPUtils.convertIPStringToInetAddress(serverLocalIpAddress));
		response.setSecs((short) (request.getSecs() + 1));

		response.setAddress(IPUtils.convertIPStringToInetAddress(serverLocalIpBroadcast));

		Collection<DHCPOption> options = new ArrayList<DHCPOption>();
		options.add(DHCPOption.newOptionAsByte(DHCPConstants.DHO_DHCP_MESSAGE_TYPE, DHCPConstants.DHCPACK));
		options.add(DHCPOption.newOptionAsInetAddress(DHCPConstants.DHO_DHCP_SERVER_IDENTIFIER,
				IPUtils.convertIPStringToInetAddress(serverLocalIpAddress)));
		options.add(DHCPOption.newOptionAsInt(DHCPConstants.DHO_DHCP_LEASE_TIME, leaseTime));
		options.add(DHCPOption.newOptionAsInt(DHCPConstants.DHO_DHCP_RENEWAL_TIME, renewalTime));
		options.add(DHCPOption.newOptionAsInetAddress(DHCPConstants.DHO_SUBNET_MASK,
				IPUtils.convertIPStringToInetAddress(serverLocalIpMask)));

		response.setOptions(options);

		Port port = new Port();
		port.setMacAddress(macAddress);
		port.setIpAddress(requestedAddress.getHostAddress());

		eventService.publish(SonarTopics.TOPIC_DHCP_IP_ASSIGNED, port);

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
}
