package br.ufu.facom.mehar.sonar.dhcp.engine;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import br.ufu.facom.mehar.sonar.core.util.IPUtils;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPConstants;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPOption;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPPacket;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPServlet;
import br.ufu.facom.mehar.sonar.dhcp.service.PoolService;

@Component
public class DHCPEngine extends DHCPServlet {
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
	
	Logger subcassLogger = LoggerFactory.getLogger(DHCPEngine.class);
	
	@Override
	protected DHCPPacket doDiscover(DHCPPacket request) {
		subcassLogger.debug("doDiscover method invoked!");
		subcassLogger.debug(request.toString());

		String macAddress = request.getChaddrAsHex();
		InetAddress offeredInetAddress = IPUtils.convertIPStringToInetAddress(poolService.getIP(macAddress));

		subcassLogger.info("Discover from " + macAddress + " offered:" + offeredInetAddress);

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
		subcassLogger.debug("doRequest method invoked!");
		subcassLogger.debug(request.toString());

		String macAddress = request.getChaddrAsHex();
		InetAddress requestedAddress = request.getOptionAsInetAddr(DHCPConstants.DHO_DHCP_REQUESTED_ADDRESS);

		subcassLogger.info("Request " + requestedAddress + " from " + macAddress);

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

		return response;
	}

	@Override
	protected DHCPPacket doInform(DHCPPacket request) {
		subcassLogger.info("doInform method no implemented yet!");
		subcassLogger.info(request.toString());
		return null;
	}

	@Override
	protected DHCPPacket doDecline(DHCPPacket request) {
		subcassLogger.info("doDecline method no implemented yet!");
		subcassLogger.info(request.toString());
		return null;
	}

	@Override
	protected DHCPPacket doRelease(DHCPPacket request) {
		subcassLogger.info("doRelease method no implemented yet!");
		subcassLogger.info(request.toString());
		return null;
	}
}
