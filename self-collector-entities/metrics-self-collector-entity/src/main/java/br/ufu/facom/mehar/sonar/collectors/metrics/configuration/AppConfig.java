package br.ufu.facom.mehar.sonar.collectors.metrics.configuration;

import java.time.Duration;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

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
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
			Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setRoutingKey(appProperties.getAmqpExchangeRoutingKey());
		rabbitTemplate.setExchange(appProperties.getAmqpExchangeName());
		rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter);
		return rabbitTemplate;
	}

	@Bean
	public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory, Exchange exchange) {
		AmqpAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
		rabbitAdmin.declareExchange(exchange);
		return new RabbitAdmin(connectionFactory());
	}

	@Bean
	public Exchange topicExchange() {
		Exchange topicExchange = new TopicExchange(appProperties.getAmqpExchangeName());
		return topicExchange;
	}

	@Bean
	public ConnectionFactory connectionFactory() {
		ConnectionFactory connectionFactory = new CachingConnectionFactory(appProperties.getAmqpBrokerIp());
		return connectionFactory;
	}

	@Bean
	public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
		Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
		return jackson2JsonMessageConverter;
	}

	@Bean
	public EventService publisher() {
		return new RabbitEventService();
	}

}
