package br.ufu.facom.mehar.sonar.core.util.openflow;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;

import br.ufu.facom.mehar.sonar.core.util.exception.OpenflowConversionException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class PacketOutRequest {

	private InetAddress destination;
	
	private OFPacketOut packetOut;
	
	private byte[] packetOutBytes;
	
	public PacketOutRequest(InetAddress destination, OFPacketOut packetOut) {
		super();
		this.destination = destination;
		this.packetOut = packetOut;
	}
	
	public PacketOutRequest(InetAddress destination, byte[] packetOutBytes) {
		super();
		this.destination = destination;
		this.packetOutBytes = packetOutBytes;
	}

	public InetAddress getDestination() {
		return destination;
	}

	public void setDestination(InetAddress destination) {
		this.destination = destination;
	}

	public byte[] getPacketOutBytes() {
		return packetOutBytes;
	}

	public void setPacketOutBytes(byte[] packetOutBytes) {
		this.packetOutBytes = packetOutBytes;
	}
	
	public void setPacketOut(OFPacketOut packetOut) {
		this.packetOut = packetOut;
	}

	public OFPacketOut getPacketOut() {
		if(packetOut == null && packetOutBytes != null) {
			return this.deserializeOFMessage();
		}
		return packetOut;
	}

	public  byte[] serialize() {
		if(packetOutBytes == null) {
			ByteBuf byteBuf = Unpooled.buffer();
			packetOut.writeTo(byteBuf);
			packetOutBytes = new byte[byteBuf.readableBytes()];
			byteBuf.getBytes(0, packetOutBytes);
		}
		
		byte[] destinationArray = destination.getAddress();
		byte sizeDestination = (byte)destinationArray.length;
		
		int length = 1 + sizeDestination + packetOutBytes.length;
		
		byte[] data = new byte[length];
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.put(sizeDestination);
		bb.put(destinationArray);
		bb.put(packetOutBytes);
		
		return data;
	}
	
	public static PacketOutRequest deserialize(byte[] data) {
		try {
			ByteBuffer bb = ByteBuffer.wrap(data);
			
			byte sizeDestination = bb.get();
			
			byte[] DestinationArray = new byte[sizeDestination];
			bb.get(DestinationArray);
			
			byte[] packetOutArray = new byte[data.length - sizeDestination - 1];
			bb.get(packetOutArray);
			
			InetAddress destination = InetAddress.getByAddress(DestinationArray);
		
			return new PacketOutRequest(destination, packetOutArray);
		
		}catch(UnknownHostException e) {
			throw new OpenflowConversionException("Unable to parse the destination of PacketOutRequest!",e);
		} 
	}
	
	private OFPacketOut deserializeOFMessage() {
		try {
			OFMessage message = OFFactories.getGenericReader().readFrom(Unpooled.wrappedBuffer(packetOutBytes));
			if( OFType.PACKET_OUT.equals(message.getType()) ){
				this.packetOut = (OFPacketOut)message;
				return this.packetOut;
			}else {
				throw new OpenflowConversionException("OFMessage is not a PacketOut. Instead it is "+message.getType());
			}
		} catch (OFParseError e) {
			throw new OpenflowConversionException("Unable to parse the packetOut of PacketOutRequest!",e);
		}
	}
}
