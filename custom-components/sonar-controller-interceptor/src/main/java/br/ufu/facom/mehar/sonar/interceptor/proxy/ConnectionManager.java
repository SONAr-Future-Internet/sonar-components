package br.ufu.facom.mehar.sonar.interceptor.proxy;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

public class ConnectionManager {
	//Holder for Connections
    private List<Connection> connectionList = Collections.synchronizedList(new ArrayList<Connection>());
    private Map<Channel, Connection> connectionChannelMap = Collections.synchronizedMap(new HashMap<Channel, Connection>());
    private Map<InetAddress, Connection> connectionAddressMap = Collections.synchronizedMap(new HashMap<InetAddress, Connection>());

    //Util Sequence Generator
    private AtomicInteger sequenceGenerator = new AtomicInteger(0);
    
    //Bootstrap for Client Connections
    private Bootstrap bootstrap;
    
    //Controller IP and Port
    private String controllerIP;
    private Integer controllerPort;
    
    //Connect to the Controller
	public ChannelFuture connect() {
		return this.getBootstrap().connect(this.getControllerIP(), this.getControllerPort());
	}

	//Register channel between switch and proxy
    public synchronized Connection registerUpstream(Channel newUpstream) {
        Connection connection = new Connection(sequenceGenerator.incrementAndGet());
        
        connectionChannelMap.put(newUpstream, connection);
        connectionAddressMap.put( ((InetSocketAddress) newUpstream.remoteAddress()).getAddress(), connection);
        connectionList.add(connection);

        connection.registerUpstream(newUpstream);

        return connection;
    }

    //Register channel between proxy and controller
    public synchronized Connection registerDownstream(Channel newDownstream, Channel existingUpstream) {
        Connection connection = getConnection(existingUpstream);
        connectionChannelMap.put(newDownstream, connection);

        connection.registerDownstream(newDownstream);
        
        return connection;
    }

	//Unregister channel between switch and proxy
    public synchronized void unregisterUpstream(Channel channel) {
    	connectionAddressMap.remove( ((InetSocketAddress) channel.remoteAddress()).getAddress() );
        Connection connection = connectionChannelMap.remove(channel);
        
        if (connectionList.contains(connection)){
            connectionList.remove(connection);
        }


        if (connection != null) {
            connection.unregisterUpstream();
        }
    }

    //Unregister channel between proxy and controller
    public synchronized void unregisterDownstream(Channel channel) {
        Connection connection = connectionChannelMap.remove(channel);

        if (connectionList.contains(connection)){
            connectionList.remove(connection);
        }

        if (connection != null) {
            connection.unregisterDownstream();
        }
    }
    
    //Get Connection By Channel
    public synchronized Connection getConnection(Channel channel) {
        return connectionChannelMap.get(channel);
    }
    
    //Get Connection By Adress
	public synchronized Connection getConnection(InetAddress inetAddress) {
		return connectionAddressMap.get(inetAddress);
	}

	public Bootstrap getBootstrap() {
		return bootstrap;
	}

	public void setBootstrap(Bootstrap bootstrap) {
		this.bootstrap = bootstrap;
	}

	public String getControllerIP() {
		return controllerIP;
	}

	public void setControllerIP(String controllerIP) {
		this.controllerIP = controllerIP;
	}

	public Integer getControllerPort() {
		return controllerPort;
	}

	public void setControllerPort(Integer controllerPort) {
		this.controllerPort = controllerPort;
	}

}
