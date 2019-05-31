package br.ufu.facom.mehar.sonar.interceptor.proxy;

import java.net.InetAddress;

public interface Interceptor {
	void intercept(Direction direction, InetAddress source, InetAddress destination, byte[] data, int length);
}
