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
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.IPv4Address;

import br.ufu.facom.mehar.sonar.interceptor.packet.Ethernet;
import br.ufu.facom.mehar.sonar.interceptor.packet.IPacket;
import br.ufu.facom.mehar.sonar.interceptor.proxy.Direction;
import br.ufu.facom.mehar.sonar.interceptor.proxy.Interceptor;
import io.netty.buffer.Unpooled;

public class SonarSyncInterceptor implements Interceptor{
//	private static final Logger logger = LoggerFactory.getLogger(SonarSyncInterceptor.class);
	
	private final OFFactory ofFactory = OFFactories.getFactory(OFVersion.OF_13);
	private final OFMessageReader<OFMessage> ofMessageReader = ofFactory.getReader();
	
	@Override
	public void intercept(Direction direction, InetAddress source, InetAddress destination, byte[] data, int length) {
		try {
			OFMessage message = ofMessageReader.readFrom(Unpooled.wrappedBuffer(Arrays.copyOfRange(data, 0, length)));
			
			if( OFType.PACKET_IN.equals(message.getType()) ){
				OFPacketIn packetIn = (OFPacketIn)message;
				
				
				Ethernet ethernet = new Ethernet();
				ethernet.deserialize(packetIn.getData(), 0, packetIn.getData().length);
				
				IPacket current = ethernet;
				System.out.println("---------------------------------");
				do{
					System.out.println(current.getClass());
					System.out.println(current);
					System.out.println("---------------------------------");
					current = current.getPayload();
				}while(current != null && current instanceof IPacket);
				
//						
//						packetIn.getData()
//				if(isDHCP()) {
//					System.out.println(packetIn.getMatch().get(MatchField.IN_PORT));
//					System.out.println(packetIn.getData());	
//				}else {
//					System.out.println("Not DHCP!");
//				}
//				
//				if(OFVersion.OF_13.equals(message.getVersion())){
//					
//					System.out.println("Found Packet-In");
//					System.out.println(message.getClass());
//				}else {
//					logger.error("Version of OpenFlow not implemented yet! Version:"+message.getVersion());
//				}
			}
			
			System.out.println(" ["+message.getType()+"] "+source.getHostAddress()+" -> "+destination.getHostAddress());
		} catch (OFParseError e) {
			System.out.println("Error while paring message from "+source.getHostAddress()+" to "+destination.getHostAddress()+" with "+length+" bytes.");
		}
	}

	private boolean isDHCP(OFPacketIn packetIn) {
//		IPv4Address.
		
		
		return 	packetIn.getMatch().get(MatchField.ETH_TYPE).equals("0x0800") &&
				packetIn.getMatch().get(MatchField.IP_PROTO).equals(17) &&
				packetIn.getMatch().get(MatchField.IPV4_DST).equals("255.255.255.255/32") &&
				packetIn.getMatch().get(MatchField.UDP_DST).equals(67);
	}
}
