package br.ufu.facom.mehar.sonar.core.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.ufu.facom.mehar.sonar.core.util.exception.JsonConversionException;
import br.ufu.facom.mehar.sonar.core.util.exception.ObjectCloneException;

public class ObjectUtils {
	private static ObjectMapper objectMapper = new ObjectMapper();
	static {
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Object> T clone(T object) {
		try {
			String json = objectMapper.writeValueAsString(object);
			return (T) objectMapper.readValue(json, object.getClass());
		} catch (IOException e) {
			throw new ObjectCloneException("Error cloning object: "+ ObjectUtils.toString(object));
		}
	}
	
	public static String toString(Object object) {
		return ToStringBuilder.reflectionToString(object);
	}
	
	
	public static String fromObject(Object obj, String...excludes) {
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
	
	public static <T extends Object> T toObject(String json, Class<T> type) {
		try {
			return objectMapper.readValue(json, type);
		} catch (IOException e) {
			throw new JsonConversionException("Error serializing object of class "+type+".",e);
		}
	}
}
