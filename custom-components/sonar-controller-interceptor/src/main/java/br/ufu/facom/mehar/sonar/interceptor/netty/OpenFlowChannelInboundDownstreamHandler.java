package br.ufu.facom.mehar.sonar.interceptor.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufu.facom.mehar.sonar.interceptor.filter.Interceptor;
import br.ufu.facom.mehar.sonar.interceptor.openflow.OFMessageHolder;
import br.ufu.facom.mehar.sonar.interceptor.proxy.Connection;
import br.ufu.facom.mehar.sonar.interceptor.proxy.ConnectionManager;
import io.netty.channel.ChannelHandlerContext;

class OpenFlowChannelInboundDownstreamHandler extends OpenFlowChannelInboundHandler {
	Logger logger = LoggerFactory.getLogger(OpenFlowChannelInboundDownstreamHandler.class);
	
	public OpenFlowChannelInboundDownstreamHandler(ConnectionManager connectionManager, Interceptor interceptor) {
        super(connectionManager, interceptor);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        //Considering that the channel is already registered in ConnectionManager as a "reverse" channel...
        Connection proxiedConnection = connectionManager.getConnection(ctx.channel());
        
        if(proxiedConnection != null) {
        	//... just activate it to allow the transmission of messages
        	proxiedConnection.activeDownstream();
        }else {
        	logger.error("Coudn't find a proxied connection for channel "+ctx.channel()+" - "+(ctx.channel().localAddress() != null? ctx.channel().localAddress(): "")+" -> "+(ctx.channel().remoteAddress() != null? ctx.channel().remoteAddress(): ""));
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, OFMessageHolder messageHolder) throws Exception {
        if(this.interceptor.intercept(channelHandlerContext, messageHolder)) {
        	super.channelRead0(channelHandlerContext, messageHolder);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        //Unregister the channel in connectionManager
        connectionManager.unregisterDownstream(ctx.channel());
    }
}