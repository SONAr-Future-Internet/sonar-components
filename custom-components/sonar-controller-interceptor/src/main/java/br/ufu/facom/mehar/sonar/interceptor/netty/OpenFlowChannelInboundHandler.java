package br.ufu.facom.mehar.sonar.interceptor.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufu.facom.mehar.sonar.interceptor.filter.Interceptor;
import br.ufu.facom.mehar.sonar.interceptor.openflow.OFMessageHolder;
import br.ufu.facom.mehar.sonar.interceptor.proxy.ChannelType;
import br.ufu.facom.mehar.sonar.interceptor.proxy.Connection;
import br.ufu.facom.mehar.sonar.interceptor.proxy.ConnectionManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

abstract class OpenFlowChannelInboundHandler extends SimpleChannelInboundHandler<OFMessageHolder> {
	private static final Logger logger = LoggerFactory.getLogger(OpenFlowChannelInboundHandler.class);
	
    //Manager of Connections
	protected ConnectionManager connectionManager;
	
	//Interceptor/Filter of Messages
	protected Interceptor interceptor;

    public OpenFlowChannelInboundHandler(ConnectionManager connectionManager, Interceptor interceptor) {
        this.connectionManager = connectionManager;
        this.interceptor = interceptor;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);

        //In case of idle...
        if (evt instanceof IdleStateEvent) {
        	//get the connection from ConnectionManager using the channel
            Connection connection = connectionManager.getConnection(ctx.channel());

            //If the connection is correctly established...
            if (connection.getDownstreamVersion() != null && connection.getUpstreamVersion() != null) {
            	//If the idle event is referred to the 'reader'...
                IdleStateEvent e = (IdleStateEvent) evt;
                if (e.state() == IdleState.READER_IDLE) {
                	//.. and no messages have been received in a long time, so the connection should be closed
                    logger.info("Read timeout in " + ctx.channel().remoteAddress() + ".");
                    ctx.close();
                } else if (e.state() == IdleState.WRITER_IDLE) {
                    //If not, send a ICMP ECHO request considering the OFVersion of the channel
                    connection.send(ChannelType.PROXY, ctx.channel(), connection.createPing());
                }
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, OFMessageHolder container) throws Exception {
    	//Get the connection from ConnectionManager using the channel and place the MessageHolder
        Connection connection = connectionManager.getConnection(channelHandlerContext.channel());
        connection.receive(channelHandlerContext.channel(), container);
    }
}