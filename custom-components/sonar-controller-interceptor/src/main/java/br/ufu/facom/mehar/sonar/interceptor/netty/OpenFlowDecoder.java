package br.ufu.facom.mehar.sonar.interceptor.netty;

import java.util.List;

import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFMessage;

import br.ufu.facom.mehar.sonar.interceptor.openflow.OFMessageHeader;
import br.ufu.facom.mehar.sonar.interceptor.openflow.OFMessageHolder;
import br.ufu.facom.mehar.sonar.interceptor.openflow.OFMessagePayload;
import br.ufu.facom.mehar.sonar.interceptor.openflow.OFMessageType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class OpenFlowDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> objects) throws Exception {
    	//Decode the ByteBuf into a OFMessageHolder
        OFMessageHolder container = decode(byteBuf);

        //Add messageHolder to objects list to be processed in pipeline
        objects.add(container);
    }

	public OFMessageHolder decode(ByteBuf byteBuf) throws OFParseError {
		//Check if the bytebuf has at least the 8 bytes of header
        if (byteBuf.readableBytes() < 8) {
            throw new IllegalStateException();
        }

        //Read header fields
        short version = byteBuf.readUnsignedByte();
        short typeId = byteBuf.readUnsignedByte();
        int length = byteBuf.readUnsignedShort();
        long transactionId = byteBuf.readUnsignedInt();
        
        //Build OFHeader and identify OFType
        OFMessageType type = OFMessageType.getById(typeId);
        OFMessageHeader header = new OFMessageHeader(version, typeId, length, transactionId);

        //Recover all bytes (including header) to an array of bytes
        byteBuf.resetReaderIndex();
        byte[] payloadData = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(byteBuf.readerIndex(), payloadData);
        
        //Build OFPayload
        OFMessagePayload payload = new OFMessagePayload(payloadData);

        //Build OFMessage using a generic reader
        byteBuf.resetReaderIndex();
        OFMessage message = OFFactories.getGenericReader().readFrom(byteBuf);
        
        //Build OFMessageHolder using OFHeader, OFPayload, OFType and OFMessage
        OFMessageHolder container = new OFMessageHolder(header, payload, type, message);
        
		return container;
	}
}