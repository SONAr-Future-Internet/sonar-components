package br.ufu.facom.mehar.sonar.interceptor.filter;

import br.ufu.facom.mehar.sonar.interceptor.openflow.OFMessageHolder;
import io.netty.channel.ChannelHandlerContext;

public interface Interceptor {
	Boolean intercept(ChannelHandlerContext channelHandlerContext, OFMessageHolder messageHolder);
}
