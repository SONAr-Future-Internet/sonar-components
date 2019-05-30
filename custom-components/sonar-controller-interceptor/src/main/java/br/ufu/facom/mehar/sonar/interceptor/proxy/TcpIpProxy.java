package br.ufu.facom.mehar.sonar.interceptor.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by oksuz on 29/10/2017.
 */
public class TcpIpProxy {

    

    private final String remoteIp;
    private final int remotePort;
    private final int port;

    public TcpIpProxy(String remoteIp, int remotePort, int port) {
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
        this.port = port;
    }

    
}