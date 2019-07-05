package br.ufu.facom.mehar.sonar.interceptor.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufu.facom.mehar.sonar.client.nem.service.EventService;
import br.ufu.facom.mehar.sonar.interceptor.openflow.OFMessageHolder;
import io.netty.channel.ChannelHandlerContext;

public class AsyncInterceptor implements Interceptor {
	private static final Logger logger = LoggerFactory.getLogger(AsyncInterceptor.class);
	
	private SyncInterceptor syncInterceptor;
	
	public AsyncInterceptor(EventService eventService) {
		this.syncInterceptor = new SyncInterceptor(eventService);
	}

	@Override
	public Boolean intercept(final ChannelHandlerContext channelHandlerContext, final OFMessageHolder messageHolder) {
		try {
			new Thread(new Runnable() {
				@Override
				public void run() {
					syncInterceptor.intercept(channelHandlerContext, messageHolder);
				}
			}).start();
		} catch(Exception e) {
			logger.error("Error while intercepting "+messageHolder.getMessageType()+" from "+channelHandlerContext.channel().remoteAddress(), e);
		}
		
		return Boolean.TRUE;
	}
}
