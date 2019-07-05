package br.ufu.facom.mehar.sonar.interceptor.netty;

import java.util.concurrent.TimeUnit;

import br.ufu.facom.mehar.sonar.interceptor.filter.Interceptor;
import br.ufu.facom.mehar.sonar.interceptor.proxy.ConnectionManager;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

public class OpenFlowChannelInitializer extends ChannelInitializer<SocketChannel> {
    public static int OPENFLOW_MAXIMUM_FRAME = (int) Math.pow(2, 16);

    private ConnectionManager connectionManager;
    
    private Interceptor interceptor;
    
    private Boolean downstream;
    
    //Max time to wait for reading or writing
    private long idleReadTimeout = 300000;
    private long idleWriteTimeout = 300000;
    
    public OpenFlowChannelInitializer(ConnectionManager connectionManager, Interceptor interceptor, Boolean downstream) {
    	super();
    	this.connectionManager = connectionManager;
    	this.interceptor = interceptor;
        this.downstream = downstream;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        //First level decoder
        pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(OPENFLOW_MAXIMUM_FRAME, 2, 2, -4, 0));

        //Decoder of segments data into OpenFlow primitives
        pipeline.addLast("openFlowDecoder", new OpenFlowDecoder());
        
        //Re-encode of OpenFlow primitives
        pipeline.addLast("openFlowEncoder", new OpenFlowEncoder());

        //Handler to Idle state
        pipeline.addLast("idleStateHandler", new IdleStateHandler(idleReadTimeout, idleWriteTimeout, 0, TimeUnit.MILLISECONDS));

        //OpenFlow Handler according to the direction of the communication
        if (downstream) {
            pipeline.addLast("messageHandler", new OpenFlowChannelInboundDownstreamHandler(connectionManager, interceptor));
        } else {
            pipeline.addLast("messageHandler", new OpenFlowChannelInboundUpstreamHandler(connectionManager, interceptor));
        }
    }
}