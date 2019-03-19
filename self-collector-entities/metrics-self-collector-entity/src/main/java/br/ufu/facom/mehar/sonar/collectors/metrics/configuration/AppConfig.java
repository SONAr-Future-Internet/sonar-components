package br.ufu.facom.mehar.sonar.collectors.metrics.configuration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
	
	@Autowired
	private AppProperties appProperties;
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		builder.setConnectTimeout(Duration.ofMillis(appProperties.getHttpConnectTimeout()));
		builder.setReadTimeout(Duration.ofMillis(appProperties.getHttpReadTimeout()));
		builder.additionalMessageConverters(new MappingJackson2HttpMessageConverter());
		return builder.build();
	}

}
