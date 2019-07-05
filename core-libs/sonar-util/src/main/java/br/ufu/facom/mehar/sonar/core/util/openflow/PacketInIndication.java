package br.ufu.facom.mehar.sonar.core.util.openflow;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFType;

import br.ufu.facom.mehar.sonar.core.util.exception.OpenflowConversionException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class PacketInIndication {

	private InetAddress source;
	
	private OFPacketIn packetIn;
	
	private byte[] packetInBytes;
	
	public PacketInIndication(InetAddress source, OFPacketIn packetIn) {
		super();
		this.source = source;
		this.packetIn = packetIn;
	}
	
	public PacketInIndication(InetAddress source, byte[] packetInBytes) {
		this.source = source;
		this.packetInBytes = packetInBytes;
	}
	
	
	public InetAddress getSource() {
		return source;
	}

	public void setSource(InetAddress source) {
		this.source = source;
	}

	public byte[] getPacketInBytes() {
		return packetInBytes;
	}

	public void setPacketInBytes(byte[] packetInBytes) {
		this.packetInBytes = packetInBytes;
	}
	
	public void setPacketIn(OFPacketIn packetIn) {
		this.packetIn = packetIn;
	}
	
	public OFPacketIn getPacketIn() {
		if(this.packetIn == null && packetInBytes != null) {
			return deserializeOFMessage(); 
		}
		return packetIn;
	}

	public  byte[] serialize() {
		if(packetInBytes == null) {
			ByteBuf byteBuf = Unpooled.buffer();
			packetIn.writeTo(byteBuf);
			packetInBytes = new byte[byteBuf.readableBytes()];
			byteBuf.getBytes(0, packetInBytes);
		}
		
		byte[] sourceArray = source.getAddress();
		
		byte sizeSource = (byte)sourceArray.length;
		
		int length= 1 + sizeSource + packetInBytes.length;
		
		byte[] data = new byte[length];
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.put(sizeSource);
		bb.put(sourceArray);
		bb.put(packetInBytes);
		
		return data;
	}
	
	public static PacketInIndication deserialize(byte[] data) {
		try {
			ByteBuffer bb = ByteBuffer.wrap(data);
			
			byte sizeSource = bb.get();
			
			byte[] sourceArray = new byte[sizeSource];
			bb.get(sourceArray);
			
			byte[] packetInArray = new byte[data.length - sizeSource - 1];
			bb.get(packetInArray);
			
			InetAddress source = InetAddress.getByAddress(sourceArray);
			
			return new PacketInIndication(source, packetInArray);
			
		}catch(UnknownHostException e) {
			throw new OpenflowConversionException("Unable to parse the source of PacketInIndication!",e);
		} 
	}
	
	private OFPacketIn deserializeOFMessage() {
		try {
			OFMessage message = OFFactories.getGenericReader().readFrom(Unpooled.wrappedBuffer(packetInBytes));
			if( OFType.PACKET_IN.equals(message.getType()) ){
				this.packetIn = (OFPacketIn)message;
				return this.packetIn;
			}else {
				throw new OpenflowConversionException("OFMessage is not a PacketIn. Instead it is "+message.getType());
			}
		} catch (OFParseError e) {
			throw new OpenflowConversionException("Unable to parse the packetIn of PacketInIndication!",e);
		}
	}
}
