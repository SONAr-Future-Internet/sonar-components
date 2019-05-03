package br.ufu.facom.mehar.sonar.collectors.metrics.configuration;

import java.time.Duration;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import br.ufu.facom.mehar.sonar.client.nem.configuration.NEMConfiguration;
import br.ufu.facom.mehar.sonar.client.nem.service.EventService;
import br.ufu.facom.mehar.sonar.client.nem.service.impl.rabbitmq.RabbitEventService;

@Configuration
public class AppConfig {

	@Autowired
	private AppProperties appProperties;

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		RestTemplate restTemplate;
		builder.setConnectTimeout(Duration.ofMillis(appProperties.getHttpConnectTimeout()));
		builder.setReadTimeout(Duration.ofMillis(appProperties.getHttpReadTimeout()));
		builder.additionalMessageConverters(new MappingJackson2HttpMessageConverter());
		restTemplate = builder.build();
		restTemplate.getInterceptors()
				.add(new BasicAuthenticationInterceptor(appProperties.getOnosUser(), appProperties.getOnosPassword()));
		return restTemplate;
	}

	@Bean
	public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
		Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
		return jackson2JsonMessageConverter;
	}

	@Bean
	public NEMConfiguration nemConfiguration() {
		return new NEMConfiguration();
	}

	@Bean
	public EventService publisher() {
		return new RabbitEventService();
	}

}
