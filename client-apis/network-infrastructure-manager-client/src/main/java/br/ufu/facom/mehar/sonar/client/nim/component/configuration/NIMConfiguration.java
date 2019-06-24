package br.ufu.facom.mehar.sonar.client.nim.component.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Configuration
public class NIMConfiguration {

	@Bean
	public RestTemplate restTemplate() {
		RestTemplate template = new RestTemplate();
		
		for(HttpMessageConverter<?> converter : template.getMessageConverters()) {
			if(converter.getClass().getName().equals(MappingJackson2HttpMessageConverter.class.getName())) {
				((MappingJackson2HttpMessageConverter)converter).setPrettyPrint(true);
				((MappingJackson2HttpMessageConverter)converter).getObjectMapper().setSerializationInclusion(Include.NON_NULL);
				((MappingJackson2HttpMessageConverter)converter).getObjectMapper().setSerializationInclusion(Include.NON_NULL);
			}
		}
		return template;
	}
}
