package br.ufu.facom.mehar.sonar.interceptor.netty;

import java.util.List;

import br.ufu.facom.mehar.sonar.interceptor.openflow.OFMessageHolder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

class OpenFlowEncoder extends MessageToMessageEncoder<OFMessageHolder> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, OFMessageHolder messageHolder, List<Object> objects) throws Exception {
        //Create a ByteBuf using the length defined in OFHeader
        ByteBuf output = channelHandlerContext.alloc().buffer(messageHolder.getHeader().getLength());

        //Write OFPayload data into the ByteBuf
        output.writeBytes(messageHolder.getPayload().getData());

        //Add ByteBuf to objects list to be processed in pipeline
        objects.add(output);
    }
}