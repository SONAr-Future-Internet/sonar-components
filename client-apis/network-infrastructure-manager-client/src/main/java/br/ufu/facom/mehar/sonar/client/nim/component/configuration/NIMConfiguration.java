package br.ufu.facom.mehar.sonar.client.nim.component.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class NIMConfiguration {

	@Bean
	public RestTemplate restTemplate() {
	    return new RestTemplate();
	}
}
