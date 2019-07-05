package br.ufu.facom.mehar.sonar.interceptor.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufu.facom.mehar.sonar.interceptor.filter.Interceptor;
import br.ufu.facom.mehar.sonar.interceptor.openflow.OFMessageHolder;
import br.ufu.facom.mehar.sonar.interceptor.proxy.ConnectionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.GenericFutureListener;

class OpenFlowChannelInboundUpstreamHandler extends OpenFlowChannelInboundHandler {
	private static final Logger logger = LoggerFactory.getLogger(OpenFlowChannelInboundUpstreamHandler.class);

	public OpenFlowChannelInboundUpstreamHandler(ConnectionManager connectionManager, Interceptor interceptor) {
        super(connectionManager, interceptor);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        //Register the channel in ConnectionManager
        final Channel upstreamChannel = ctx.channel();
        connectionManager.registerUpstream(upstreamChannel);

        //Request a connection to the server
        ChannelFuture future = connectionManager.connect();

        //Register reverse channel into ConnectionManager
        final Channel downstreamChannel = future.channel();
        connectionManager.registerDownstream(downstreamChannel, upstreamChannel);

        //Wait until the connection is established or failed
        future.awaitUninterruptibly();
        
        //When the Future is resolved
        future.addListener(new GenericFutureListener<ChannelFuture>() {
			@Override
			public void operationComplete(ChannelFuture channelFuture) throws Exception {
				//If the connection to the server fails
				if (!channelFuture.isSuccess()) {
					logger.error("Failed to create a downstream from the server.",channelFuture.cause() );
					
					//Close the channel
	                upstreamChannel.close();
	            }
			}
        	
		});
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
        connectionManager.unregisterUpstream(ctx.channel());
    }
}