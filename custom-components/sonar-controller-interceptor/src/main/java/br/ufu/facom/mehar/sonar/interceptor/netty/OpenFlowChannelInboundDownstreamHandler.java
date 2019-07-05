package br.ufu.facom.mehar.sonar.interceptor.netty;

import br.ufu.facom.mehar.sonar.interceptor.filter.Interceptor;
import br.ufu.facom.mehar.sonar.interceptor.openflow.OFMessageHolder;
import br.ufu.facom.mehar.sonar.interceptor.proxy.Connection;
import br.ufu.facom.mehar.sonar.interceptor.proxy.ConnectionManager;
import io.netty.channel.ChannelHandlerContext;

class OpenFlowChannelInboundDownstreamHandler extends OpenFlowChannelInboundHandler {
	
	public OpenFlowChannelInboundDownstreamHandler(ConnectionManager connectionManager, Interceptor interceptor) {
        super(connectionManager, interceptor);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        //Considering that the channel is already registered in ConnectionManager as a "reverse" channel...
        Connection proxiedConnection = connectionManager.getConnection(ctx.channel());
        
        //... just activate it to allow the transmission of messages
        proxiedConnection.activeDownstream();
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