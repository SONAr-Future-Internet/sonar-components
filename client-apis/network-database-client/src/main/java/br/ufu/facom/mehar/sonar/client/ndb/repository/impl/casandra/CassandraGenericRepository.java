package br.ufu.facom.mehar.sonar.client.ndb.repository.impl.casandra;

import java.net.InetSocketAddress;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.NoHostAvailableException;

import br.ufu.facom.mehar.sonar.client.ndb.configuration.NDBConfiguration;

public abstract class CassandraGenericRepository {
	
	private static final Logger logger = LoggerFactory.getLogger(CassandraGenericRepository.class);
	
	private static volatile Cluster cluster;
	
	@Autowired
	private NDBConfiguration configuration;
	
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
	
	public static void clusterFinish() {
		if(cluster != null) {
			cluster.close();
		}
		cluster = null;
	}
	
	public Session session() {
		try {
			if(cluster == null) {
				clusterStart();
			}
			
			return cluster.connect();
		}catch(NoHostAvailableException e) {
			if(cluster != null && !cluster.isClosed()) {
				cluster.close();
			}
			cluster = null;
			throw e;
		}
	}
	
	protected void close(Session session) {
		if(session != null) {
			session.close();
		}
		
//		clusterFinish();
	}
	
	@PreDestroy
	private void finish() {
		logger.debug("Closing connection to RabbitMQ!");
		clusterFinish();
	}
}
