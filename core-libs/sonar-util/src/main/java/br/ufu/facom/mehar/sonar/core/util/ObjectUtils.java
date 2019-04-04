package br.ufu.facom.mehar.sonar.core.util;

import java.io.IOException;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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
}
