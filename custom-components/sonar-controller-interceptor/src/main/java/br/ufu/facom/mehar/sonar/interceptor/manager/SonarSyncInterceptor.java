package br.ufu.facom.mehar.sonar.interceptor.manager;

import java.net.InetAddress;
import java.util.Arrays;

import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFMessageReader;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufu.facom.mehar.sonar.client.nem.configuration.SonarTopics;
import br.ufu.facom.mehar.sonar.client.nem.service.EventService;
import br.ufu.facom.mehar.sonar.core.util.openflow.PacketInIndication;
import br.ufu.facom.mehar.sonar.core.util.packet.DHCP;
import br.ufu.facom.mehar.sonar.core.util.packet.Ethernet;
import br.ufu.facom.mehar.sonar.core.util.packet.IPacket;
import br.ufu.facom.mehar.sonar.core.util.packet.IPv4;
import br.ufu.facom.mehar.sonar.core.util.packet.UDP;
import br.ufu.facom.mehar.sonar.interceptor.proxy.Direction;
import br.ufu.facom.mehar.sonar.interceptor.proxy.Interceptor;
import io.netty.buffer.Unpooled;

public class SonarSyncInterceptor implements Interceptor{
	private static final Logger logger = LoggerFactory.getLogger(SonarSyncInterceptor.class);
	
	private final OFFactory ofFactory = OFFactories.getFactory(OFVersion.OF_13);
	private final OFMessageReader<OFMessage> ofMessageReader = ofFactory.getReader();
	
	private final EventService eventService;
	
	public SonarSyncInterceptor(EventService eventService) {
		this.eventService = eventService;
	}

	@Override
	public void intercept(Direction direction, InetAddress source, InetAddress destination, byte[] data, int length) {
		try {
			OFMessage message = ofMessageReader.readFrom(Unpooled.wrappedBuffer(Arrays.copyOfRange(data, 0, length)));
			
			if(message != null) {
				if( OFType.PACKET_IN.equals(message.getType()) ){
					OFPacketIn packetIn = (OFPacketIn)message;
					
					
					Ethernet frame = new Ethernet();
					frame.deserialize(packetIn.getData(), 0, packetIn.getData().length);
					
//					printEthernetFrame(frame);
					
					if(isDHCP(frame)) {
						this.eventService.publish(SonarTopics.TOPIC_INTERCEPTOR_PACKET_IN_DHCP, new PacketInIndication(source, Arrays.copyOfRange(data, 0, length)).serialize());
					}
				}
				
				logger.info(" ["+message.getType()+"] "+source.getHostAddress()+" -> "+destination.getHostAddress());
			}
		} catch (OFParseError e) {
			logger.error("Error while parsing message from "+source.getHostAddress()+" to "+destination.getHostAddress()+" with "+length+" bytes.",e);
		}
	}

//	private void printEthernetFrame(Ethernet frame) {
//		IPacket current = frame;
//		System.out.println("---------------------------------");
//		do{
//			System.out.println(current.getClass());
//			System.out.println(current);
//			System.out.println("---------------------------------");
//			current = current.getPayload();
//		}while(current != null && current instanceof IPacket);
//	}

	private boolean isDHCP(Ethernet packet) {
		return EthType.IPv4.equals(packet.getEtherType()) && packet.getPayload() != null && (packet.getPayload() instanceof IPv4) &&
				IpProtocol.UDP.equals(((IPv4)packet.getPayload()).getProtocol()) && packet.getPayload().getPayload() != null && (packet.getPayload().getPayload() instanceof UDP) &&
				packet.getPayload().getPayload().getPayload() != null && packet.getPayload().getPayload().getPayload() instanceof DHCP;
	}
}
