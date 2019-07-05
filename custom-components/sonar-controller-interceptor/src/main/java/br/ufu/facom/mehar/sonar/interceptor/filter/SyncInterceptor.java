package br.ufu.facom.mehar.sonar.interceptor.filter;

import java.net.InetSocketAddress;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufu.facom.mehar.sonar.client.nem.configuration.SonarTopics;
import br.ufu.facom.mehar.sonar.client.nem.service.EventService;
import br.ufu.facom.mehar.sonar.core.util.IPUtils;
import br.ufu.facom.mehar.sonar.core.util.openflow.PacketInIndication;
import br.ufu.facom.mehar.sonar.core.util.packet.DHCP;
import br.ufu.facom.mehar.sonar.core.util.packet.Ethernet;
import br.ufu.facom.mehar.sonar.core.util.packet.IPv4;
import br.ufu.facom.mehar.sonar.core.util.packet.UDP;
import br.ufu.facom.mehar.sonar.interceptor.openflow.OFMessageHolder;
import io.netty.channel.ChannelHandlerContext;

public class SyncInterceptor implements Interceptor{
	private static final Logger logger = LoggerFactory.getLogger(SyncInterceptor.class);
	
	private final EventService eventService;
	
	public SyncInterceptor(EventService eventService) {
		this.eventService = eventService;
	}
	
	@Override
	public Boolean intercept(ChannelHandlerContext channelHandlerContext, OFMessageHolder messageHolder) {
		try {
			OFMessage message = messageHolder.getMessage();
			if(message != null) {
				InetSocketAddress sourceAddress = (InetSocketAddress)channelHandlerContext.channel().remoteAddress();
				
				logger.info(" ["+message.getType()+"] from "+IPUtils.convertInetToIPString(sourceAddress.getAddress()));
				
				if( OFType.PACKET_IN.equals(message.getType()) ){
					OFPacketIn packetIn = (OFPacketIn)message;
					
					
					Ethernet frame = new Ethernet();
					frame.deserialize(packetIn.getData(), 0, packetIn.getData().length);
					
					
					if(isDHCP(frame)) {
						this.eventService.publish(SonarTopics.TOPIC_INTERCEPTOR_PACKET_IN_DHCP, new PacketInIndication(sourceAddress.getAddress(), messageHolder.getPayload().getData()).serialize());
					}
				}
			}
		} catch(Exception e) {
			logger.error("Error while intercepting "+messageHolder.getMessageType()+" from "+channelHandlerContext.channel().remoteAddress(), e);
		}
		
		return Boolean.TRUE;
	}

	private Boolean isDHCP(Ethernet packet) {
		return EthType.IPv4.equals(packet.getEtherType()) && packet.getPayload() != null && (packet.getPayload() instanceof IPv4) &&
				IpProtocol.UDP.equals(((IPv4)packet.getPayload()).getProtocol()) && packet.getPayload().getPayload() != null && (packet.getPayload().getPayload() instanceof UDP) &&
				packet.getPayload().getPayload().getPayload() != null && packet.getPayload().getPayload().getPayload() instanceof DHCP;
	}

	
}
