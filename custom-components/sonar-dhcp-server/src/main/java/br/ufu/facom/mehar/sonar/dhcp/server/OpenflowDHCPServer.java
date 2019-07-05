package br.ufu.facom.mehar.sonar.dhcp.server;

import java.net.DatagramPacket;
import java.util.Arrays;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
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
import br.ufu.facom.mehar.sonar.core.util.IPUtils;
import br.ufu.facom.mehar.sonar.core.util.exception.OpenflowConversionException;
import br.ufu.facom.mehar.sonar.core.util.openflow.PacketInIndication;
import br.ufu.facom.mehar.sonar.core.util.openflow.PacketOutRequest;
import br.ufu.facom.mehar.sonar.core.util.packet.DHCP;
import br.ufu.facom.mehar.sonar.core.util.packet.Ethernet;
import br.ufu.facom.mehar.sonar.core.util.packet.IPv4;
import br.ufu.facom.mehar.sonar.core.util.packet.UDP;
import br.ufu.facom.mehar.sonar.dhcp.engine.OpenflowDHCPEngine;

@Component
public class OpenflowDHCPServer {
	Logger logger = LoggerFactory.getLogger(OpenflowDHCPServer.class);
	
	@Autowired
	private OpenflowDHCPEngine dhcpServlet;
	
	@Autowired
	private EventService eventService;
	
	@Value("${sonar.server.local.ip.address:192.168.0.1}")
	private String serverLocalIpAddress;
	
	
	@Value("${sonar.server.local.ip.broadcast:192.168.0.255}")
	private String serverLocalIpBroadcast;
	

	@EventListener(ApplicationReadyEvent.class)
	public void run() {
		logger.info("Starting Openflow DHCP Server...");
		logger.info("  - listening to topic: "+SonarTopics.TOPIC_INTERCEPTOR_PACKET_IN_DHCP);
		eventService.subscribe(SonarTopics.TOPIC_INTERCEPTOR_PACKET_IN_DHCP, new NetworkEventAction() {
			@Override
			public void handle(String event, byte[] payload) {
				try {
					//Build PacketInIndication Object
					PacketInIndication packetInIndication = PacketInIndication.deserialize(payload);
					
					//Get PacketIn Message
					OFPacketIn packetIn = packetInIndication.getPacketIn();
					
					//Mount PacketIn Payload as an Ethernet frame and validate all payloads until layer 5 (DHCP)
					Ethernet frame = new Ethernet();
					frame.deserialize(packetIn.getData(), 0, packetIn.getData().length);
					if(frame.getPayload() != null && frame.getPayload() instanceof IPv4) {
						IPv4 packet = (IPv4)frame.getPayload();
						if(packet.getPayload() != null && packet.getPayload() instanceof UDP) {
							UDP datagram = (UDP)packet.getPayload();
							if(datagram.getPayload() != null && datagram.getPayload() instanceof DHCP) {
								DHCP dhcpMessage = (DHCP)datagram.getPayload();
								
								//Serialize DHCP request (get bytes)
								byte[] datagramRequestData = dhcpMessage.serialize();
								
								//Build a standard DatagramRequest with the DHCP request data
								DatagramPacket requestDatagram = new DatagramPacket(datagramRequestData, datagramRequestData.length, packet.getDestinationAddress().toInetAddress(), datagram.getDestinationPort().getPort());
								
								//Recover inPort from PacketIn
								OFPort inPort =  packetIn.getMatch().get(MatchField.IN_PORT);
								
								//Call specific serviceDatagram of DHCPServlet with portInSwitch and portInPort to process the DHCP DatagramPacket Request built.
								DatagramPacket responseDatagram = dhcpServlet.serviceDatagram(IPUtils.convertInetToIPString(packetInIndication.getSource()), Integer.toString(inPort.getPortNumber()) ,requestDatagram);
								
								//If response is valid
								if(responseDatagram != null) {
									//Build the packetOut payload: create all layers from 2 to 5
									Ethernet l2 = new Ethernet();
									l2.setSourceMACAddress(MacAddress.of("00:00:00:00:00:01"));
									l2.setDestinationMACAddress(MacAddress.BROADCAST);
									l2.setEtherType(EthType.IPv4);
									
									IPv4 l3 = new IPv4();
									l3.setSourceAddress(serverLocalIpAddress);
									l3.setDestinationAddress(serverLocalIpBroadcast);
									l3.setTtl((byte) 64);
									l3.setProtocol(IpProtocol.UDP);
									
									UDP l4 = new UDP();
									l4.setSourcePort(datagram.getDestinationPort());
									l4.setDestinationPort(datagram.getSourcePort());
									
									DHCP l5 = new DHCP();
									byte[] datagramResponseData = responseDatagram.getData();
									l5.deserialize(datagramResponseData, 0, datagramResponseData.length);

									l2.setPayload(l3);
									l3.setPayload(l4);
									l4.setPayload(l5);
									
									byte[] serializedData = l2.serialize();
									
									// Create output action to send the packetOut to the original port of packetIn
									OFActionOutput output = OFFactories.getFactory(packetIn.getVersion()).actions().buildOutput()
										    .setPort(inPort)
										    .build();
									
									//Build the packetOut
									OFPacketOut packetOut = OFFactories.getFactory(packetIn.getVersion()).buildPacketOut()
											.setData(serializedData)
											.setBufferId(OFBufferId.NO_BUFFER)
											.setInPort(OFPort.CONTROLLER)
											.setActions(Arrays.asList((OFAction)output))
											.build();
									
									//Create PacketOutRequest and serialize data
									PacketOutRequest packetOutRequest = new PacketOutRequest(packetInIndication.getSource(), packetOut);
									byte[] packetOutRequestData = packetOutRequest.serialize();
									
									//Call PacketOut routine from Interceptor
									eventService.publish(SonarTopics.TOPIC_INTERCEPTOR_CALL_PACKET_OUT, packetOutRequestData);
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
