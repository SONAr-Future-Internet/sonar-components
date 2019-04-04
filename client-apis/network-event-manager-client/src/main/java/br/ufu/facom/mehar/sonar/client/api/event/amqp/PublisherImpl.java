package br.ufu.facom.mehar.sonar.client.api.event.amqp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PublisherImpl implements Publisher {

	private static final Logger LOGGER = LoggerFactory.getLogger(PublisherImpl.class);
	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Override
	public void publish(Object message) {
		LOGGER.debug("Sending message {}", message.toString());
		rabbitTemplate.convertAndSend(message);
		LOGGER.debug("Message sent}");
	}

}