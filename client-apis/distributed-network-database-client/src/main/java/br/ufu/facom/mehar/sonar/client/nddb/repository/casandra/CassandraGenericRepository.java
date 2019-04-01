package br.ufu.facom.mehar.sonar.client.nddb.repository.casandra;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.ufu.facom.mehar.sonar.client.nddb.exception.JsonConversionException;

public abstract class CassandraGenericRepository {

	private Cluster cluster;

	private ObjectMapper objectMapper = new ObjectMapper();
	{
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
	}
	
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
		return this.session(null);
	}
	
	public Session session(String keystore) {
		if(cluster == null) {
			clusterStart();
		}
		
		if(keystore != null) {
			return cluster.connect(keystore);
		}else {
			return cluster.connect();
		}
	}
	
	
	public String fromObject(Object obj, String...excludes) {
		try {
			//Save
			Map<String, Object> excludeMap = new HashMap<String, Object>();
			try {
				for(String exclude : excludes) {
					Field field = obj.getClass().getDeclaredField(exclude);
					field.setAccessible(true);
					excludeMap.put(exclude, field.get(obj));
					field.set(obj,null);
				}
				
				return objectMapper.writeValueAsString(obj);
			}finally {
				//Restore
				for(String fieldName : excludeMap.keySet()) {
					Field field = obj.getClass().getDeclaredField(fieldName);
					field.setAccessible(true);
					field.set(obj, excludeMap.get(fieldName));
				}
			}
		} catch (NoSuchFieldException | JsonProcessingException | IllegalArgumentException | IllegalAccessException e) {
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
