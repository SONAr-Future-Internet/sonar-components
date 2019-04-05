package br.ufu.facom.mehar.sonar.client.dndb.repository.impl.casandra;

import java.net.InetSocketAddress;

import org.springframework.beans.factory.annotation.Autowired;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.NoHostAvailableException;

import br.ufu.facom.mehar.sonar.client.dndb.configuration.DNDBConfiguration;

public abstract class CassandraGenericRepository {
	
	private Cluster cluster;
	
	@Autowired
	private DNDBConfiguration configuration;
	
	public void clusterStart() {
		
		Builder b =  Cluster.builder()
				.withoutMetrics();
		
		for(String seedStr : configuration.getSeeds()) {
			String ip = seedStr.split(":",2)[0].trim();
			String port = seedStr.split(":",2)[1].trim();
			b.addContactPointsWithPorts(new InetSocketAddress(ip, Integer.parseInt(port)));
		}
		
		cluster = b.build();
	}
	
	public void clusterFinish() {
		cluster.close();
		cluster = null;
	}
	
	public Session session() {
		return this.session(null);
	}
	
	public Session session(String keystore) {
		try {
			if(cluster == null) {
				clusterStart();
			}
			
			if(keystore != null) {
				return cluster.connect(keystore);
			}else {
				return cluster.connect();
			}
		}catch(NoHostAvailableException e) {
			if(cluster != null && !cluster.isClosed()) {
				cluster.close();
			}
			cluster = null;
			throw e;
		}
	}
	
	
	protected void close(Session session) {
		session.close();
		
		clusterFinish();
	}
}
