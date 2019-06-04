package br.ufu.facom.mehar.sonar.dhcp.server;

import java.net.DatagramPacket;
import java.util.Arrays;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFMessageReader;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TransportPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import br.ufu.facom.mehar.sonar.client.nem.action.NetworkEventAction;
import br.ufu.facom.mehar.sonar.client.nem.configuration.SonarTopics;
import br.ufu.facom.mehar.sonar.client.nem.service.EventService;
import br.ufu.facom.mehar.sonar.core.util.exception.OpenflowConversionException;
import br.ufu.facom.mehar.sonar.core.util.openflow.PacketInIndication;
import br.ufu.facom.mehar.sonar.core.util.openflow.PacketOutRequest;
import br.ufu.facom.mehar.sonar.core.util.packet.DHCP;
import br.ufu.facom.mehar.sonar.core.util.packet.Ethernet;
import br.ufu.facom.mehar.sonar.core.util.packet.IPv4;
import br.ufu.facom.mehar.sonar.core.util.packet.UDP;

@Component
public class OpenflowDHCPServer {
	Logger logger = LoggerFactory.getLogger(OpenflowDHCPServer.class);
	
	@Autowired
	private SONArDHCPEngine dhcpServlet;
	
	@Autowired
	private EventService eventService;
	
	@Value("${sonar.server.local.ip.address:192.168.0.1}")
	private String serverLocalIpAddress;
	
	
	@Value("${sonar.server.local.ip.broadcast:192.168.0.255}")
	private String serverLocalIpBroadcast;
	
	private final OFFactory ofFactory = OFFactories.getFactory(OFVersion.OF_13);
	private final OFMessageReader<OFMessage> ofMessageReader = ofFactory.getReader();
	

	@EventListener(ApplicationReadyEvent.class)
	public void run() {
		logger.info("Starting Openflow DHCP Server...");
		logger.info("  - listening to topic: "+SonarTopics.TOPIC_DHCP_MESSAGE_INCOMING);
		eventService.subscribe(SonarTopics.TOPIC_DHCP_MESSAGE_INCOMING, new NetworkEventAction() {
			@Override
			public void handle(String event, byte[] payload) {
				try {
//					OFMessage message = ofMessageReader.readFrom(Unpooled.wrappedBuffer(payload));
//					
//					if( OFType.PACKET_IN.equals(message.getType()) ){
					PacketInIndication packetInIndication = PacketInIndication.deserialize(payload, ofMessageReader);
					
					OFPacketIn packetIn = packetInIndication.getPacketIn();
					
					Ethernet frame = new Ethernet();
					frame.deserialize(packetIn.getData(), 0, packetIn.getData().length);
					
					if(frame.getPayload() != null && frame.getPayload() instanceof IPv4) {
						IPv4 packet = (IPv4)frame.getPayload();
						if(packet.getPayload() != null && packet.getPayload() instanceof UDP) {
							UDP datagram = (UDP)packet.getPayload();
							if(datagram.getPayload() != null && datagram.getPayload() instanceof DHCP) {
								DHCP dhcpMessage = (DHCP)datagram.getPayload();
								
								byte[] datagramRequestData = dhcpMessage.serialize();
								
								DatagramPacket requestDatagram = new DatagramPacket(datagramRequestData, datagramRequestData.length, packet.getDestinationAddress().toInetAddress(), datagram.getDestinationPort().getPort());
								DatagramPacket responseDatagram = dhcpServlet.serviceDatagram(requestDatagram);
								if(responseDatagram != null) {
									byte[] datagramResponseData = responseDatagram.getData();
									
									DHCP responseDHCP = new DHCP();
									responseDHCP.deserialize(datagramResponseData, 0, datagramResponseData.length);
									
									//Create a new packet (using request as start point)
									datagram.setPayload(responseDHCP);
									TransportPort sourcePort = datagram.getDestinationPort();
									datagram.setDestinationPort(datagram.getSourcePort());
									datagram.setSourcePort(sourcePort);
									packet.setSourceAddress(serverLocalIpAddress);
									packet.setDestinationAddress(serverLocalIpBroadcast);
									frame.setDestinationMACAddress(frame.getSourceMACAddress());
									frame.setSourceMACAddress(MacAddress.of("00:00:00:00:00:01"));
									
									
									OFPort inPort =  packetIn.getMatch().get(MatchField.IN_PORT);
									
									OFActionOutput output = ofFactory.actions().buildOutput()
										    .setPort(inPort)
										    .build();
																			
									OFPacketOut packetOut = ofFactory.buildPacketOut()
											.setData(frame.serialize())
											.setBufferId(OFBufferId.NO_BUFFER)
											.setActions(Arrays.asList((OFAction)output))
											.build();
									
//									ByteBuf byteBuf = Unpooled.buffer();
//									packetOut.writeTo(byteBuf);
//									
//									eventService.publish(SonarTopics.TOPIC_OPENFLOW_PACKET_OUT, byteBuf.array());
									
									eventService.publish(SonarTopics.TOPIC_OPENFLOW_PACKET_OUT, new PacketOutRequest(packetInIndication.getSource(), packetOut).serialize());
								}
								
							}
						}
					}
						
				} catch (OpenflowConversionException e) {
					logger.error("Error while converting Openflow Message!", e);
				}
			}
			@Override
			public void handle(String event, String json) {
				//do nothing... use "raw" handle instead
			}
		});	
	}
}
