package br.ufu.facom.mehar.sonar.core.database.client.repository.casandra;

import java.io.IOException;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.ufu.facom.mehar.sonar.core.database.client.exception.JsonConversionException;

public abstract class CassandraGenericRepository {

	private Cluster cluster;

	private ObjectMapper objectMapper = new ObjectMapper();
	
	public void clusterStart() {
		Builder b =  Cluster.builder()
				.withoutMetrics()
				.addContactPoint("127.0.0.1")
				.withPort(9042);
		cluster = b.build();
	}
	
	public void clusterFinish() {
		cluster.close();
		cluster = null;
	}
	
	public Session session() {
		if(cluster == null) {
			clusterStart();
		}
		return cluster.connect();
	}
	
	
	public String fromObject(Object obj) {
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new JsonConversionException("Error deserializing object of class "+obj.getClass()+".",e);
		}
	}
	
	public <T extends Object> T toObject(String json, Class<T> type) {
		try {
			return objectMapper.readValue(json, type);
		} catch (IOException e) {
			throw new JsonConversionException("Error serializing object of class "+type+".",e);
		}
	}
	
	protected void close(Session session) {
		session.close();
		
		clusterFinish();
	}
}
