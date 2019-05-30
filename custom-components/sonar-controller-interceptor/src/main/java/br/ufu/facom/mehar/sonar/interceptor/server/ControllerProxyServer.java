package br.ufu.facom.mehar.sonar.interceptor.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import br.ufu.facom.mehar.sonar.interceptor.configuration.CIConfiguration;
import br.ufu.facom.mehar.sonar.interceptor.proxy.Connection;
import br.ufu.facom.mehar.sonar.interceptor.proxy.TcpIpProxy;

@Component
public class ControllerProxyServer {
	private static final Logger LOGGER = LoggerFactory.getLogger(TcpIpProxy.class);

	@Value("${interceptor.proxy.port:6600}")
	private Integer port;
	
	@Autowired
	private CIConfiguration configuration;
  
	@EventListener(ApplicationReadyEvent.class)
	public void run() {
    	ServerSocket serverSocket = null;
    	Socket socket = null;
        try {
            serverSocket = new ServerSocket(port);
            LOGGER.info("Listening for connections...");
            while (true) {
                socket = serverSocket.accept();
                for(String seed : configuration.getSDNSouthSeeds()) {
                	if(seed.contains(":")) {
                		String seedParts[] = seed.split(":",2);
                		new Thread(new Connection(socket, seedParts[0], Integer.parseInt(seedParts[1]))).start();
                	}else {
                		LOGGER.error("Error while establishing connection to controller. Unable to determine seed.");
                		socket.close();
                	}
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
        	try {
        		if(socket != null) {
        			socket.close();
        		}
        		
        		if(serverSocket != null) {
        			serverSocket.close();
        		}
        	} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
}