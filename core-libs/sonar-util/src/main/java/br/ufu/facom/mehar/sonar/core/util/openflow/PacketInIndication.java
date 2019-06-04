package br.ufu.facom.mehar.sonar.core.util.openflow;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFMessageReader;
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

	public  byte[] serialize() {
		if(packetInBytes == null) {
			ByteBuf byteBuf = Unpooled.buffer();
			packetIn.writeTo(byteBuf);
			packetInBytes = byteBuf.array();
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
	
	public static PacketInIndication deserialize(byte[] data, OFMessageReader<OFMessage> ofMessageReader) {
		try {
			PacketInIndication packetInIndication = deserialize(data);
			
			OFMessage message = ofMessageReader.readFrom(Unpooled.wrappedBuffer(packetInIndication.getPacketInBytes()));
			
			if( OFType.PACKET_IN.equals(message.getType()) ){
				OFPacketIn packetIn = (OFPacketIn)message;
				packetInIndication.setPacketIn(packetIn);
				return packetInIndication;
			}else {
				throw new OpenflowConversionException("OFMessage is not a PacketIn. Instead it is "+message.getType());
			}
		} catch (OFParseError e) {
			throw new OpenflowConversionException("Unable to parse the packetIn of PacketInIndication!",e);
		}
	}

	public InetAddress getSource() {
		return source;
	}

	public void setSource(InetAddress source) {
		this.source = source;
	}

	public OFPacketIn getPacketIn() {
		return packetIn;
	}

	public void setPacketIn(OFPacketIn packetIn) {
		this.packetIn = packetIn;
	}

	public byte[] getPacketInBytes() {
		return packetInBytes;
	}

	public void setPacketInBytes(byte[] packetInBytes) {
		this.packetInBytes = packetInBytes;
	}
	
	
}
