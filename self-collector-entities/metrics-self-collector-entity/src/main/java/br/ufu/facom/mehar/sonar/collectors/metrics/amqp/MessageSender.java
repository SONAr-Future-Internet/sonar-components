package br.ufu.facom.mehar.sonar.collectors.metrics.amqp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.collectors.metrics.configuration.AppProperties;
import br.ufu.facom.mehar.sonar.collectors.metrics.model.Statistics;

@Service
public class MessageSender {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageSender.class);
	@Autowired
	private AppProperties appProperties;
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	public void publish(Statistics message) {
		LOGGER.debug("Sending message {} to {}: {}", message.toString(), appProperties.getAmqpExchangeType(),
				appProperties.getAmqpExchangeName());
		rabbitTemplate.convertAndSend(message);
		LOGGER.debug("Message sent to {}: {}", appProperties.getAmqpExchangeType(),
				appProperties.getAmqpExchangeName());
	}

}
