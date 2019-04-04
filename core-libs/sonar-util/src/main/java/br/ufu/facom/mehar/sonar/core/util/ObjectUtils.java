package br.ufu.facom.mehar.sonar.core.util;

import org.springframework.util.SerializationUtils;

public class ObjectUtils {
	
	@SuppressWarnings("unchecked")
	public static <T extends Object> T clone(T object) {
		return (T)SerializationUtils.deserialize(SerializationUtils.serialize(object));
	}
}
