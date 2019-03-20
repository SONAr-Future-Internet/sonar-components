package br.ufu.facom.mehar.sonar.boot;

import java.io.IOException;

import org.apache.directory.server.dhcp.netty.DhcpServer;
import org.apache.directory.server.dhcp.service.manager.LeaseManager;
import org.apache.directory.server.dhcp.service.store.FixedStoreLeaseManager;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		LeaseManager leaseManager = new FixedStoreLeaseManager();
		DhcpServer server = new DhcpServer(leaseManager);
		
		try {
			server.addDefaultInterfaces();
			
//			server.
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
