package br.ufu.facom.mehar.sonar.client.nem.service.impl.rabbitmq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;

import br.ufu.facom.mehar.sonar.client.nem.action.NetworkEventAction;
import br.ufu.facom.mehar.sonar.client.nem.exception.ChannelFailureException;
import br.ufu.facom.mehar.sonar.client.nem.exception.ConnectionCloseException;
import br.ufu.facom.mehar.sonar.client.nem.exception.ConnectionFailureException;
import br.ufu.facom.mehar.sonar.client.nem.exception.PublishErrorException;
import br.ufu.facom.mehar.sonar.client.nem.exception.SubscribeErrorException;
import br.ufu.facom.mehar.sonar.client.nem.service.EventService;
import br.ufu.facom.mehar.sonar.core.util.ObjectUtils;

@Service
public class RabbitEventService extends EventService {

	private static final Logger logger = LoggerFactory.getLogger(RabbitEventService.class);

	private static volatile Connection connection;

	private static final String RABBIT_LOCK = "RabbitEventServiceLock";
	
	@Override
	public void publish(String topic, byte[] payload) {
		synchronized (RABBIT_LOCK) {
			Connection connection = connection();
			try {
				Channel channel = channel(connection);
				try {
					// Declare topic
					String exchangeName = inferExchange(topic);
					channel.exchangeDeclare(exchangeName, "topic");

					// Send message
					channel.basicPublish(exchangeName, topic, null, payload);
				} finally {
					close(channel);
				}

			} catch (IOException e) {
				throw new PublishErrorException("Error publishing raw message with payload of " + payload.length + " bytes to topic '" + topic + "'.", e);
			} finally {
				// Should not close automatically
				// close(connection);
			}
		}
	}

	@Override
	public void subscribe(final String topic, final NetworkEventAction action) {
		synchronized (RABBIT_LOCK) {
			Connection connection = connection();
			try {
				Channel channel = channel(connection);
				try {
					// Declare topic
					String exchangeName = inferExchange(topic);
					channel.exchangeDeclare(exchangeName, "topic");

					// Create an anonymous queue with auto-destroy
					String queueName = channel.queueDeclare().getQueue();
					channel.queueBind(queueName, exchangeName, topic);

					Boolean autoAck = true;

					// Receive message
					channel.basicConsume(queueName, autoAck, new DeliverCallback() {
						@Override
						public void handle(String consumerTag, Delivery delivery) throws IOException {
							action.handle(delivery.getEnvelope().getRoutingKey(), delivery.getBody());
						}
					}, new CancelCallback() {
						@Override
						public void handle(String arg) throws IOException {
							logger.error("Error subscribing to topic '" + topic + "'. Cause:" + arg);
						}
					});
				} finally {
					// Should not close automatically
					// close(channel);
				}

			} catch (IOException e) {
				throw new SubscribeErrorException("Error subscribing to topic '" + topic + "'.", e);
			} finally {
				// Should not close automatically
				// close(connection);
			}
		}
	}

	/*
	 * Connection and Channel Management
	 */
	private Connection connection() {
		if (connection == null || !connection.isOpen()) {
			logger.debug("Opening connection to RabbitMQ!");
			ConnectionFactory factory = new ConnectionFactory();
			List<Address> addressList = new ArrayList<Address>();
			for (String seedStr : configuration.getSeeds()) {
				String ip = seedStr.split(":", 2)[0].trim();
				String port = seedStr.split(":", 2)[1].trim();
				addressList.add(new Address(ip, Integer.parseInt(port)));
			}

			try {
				connection = factory.newConnection(addressList);
			} catch (IOException | TimeoutException e) {
				throw new ConnectionFailureException(
						"Error connecting to rabbitmq using seeds:" + ObjectUtils.toString(configuration.getSeeds()),
						e);
			}
		}
		return connection;
	}

	private Channel channel(Connection connection) {
		try {
			return connection.createChannel();
		} catch (IOException e) {
			throw new ChannelFailureException("Error openning channel", e);
		}
	}

	@PreDestroy
	private void closeConnection() {
		logger.debug("Closing connection to RabbitMQ!");
		this.close(connection);
	}

	private void close(Connection connection) {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (IOException e) {
			throw new ConnectionCloseException("Error closing connection.", e);
		}
	}

	private void close(Channel channel) {
		try {
			if (channel != null) {
				channel.close();
			}
		} catch (TimeoutException | IOException e) {
			throw new ConnectionCloseException("Error channel connection.", e);
		}
	}

	private String inferExchange(String topic) {
		String[] parts = topic.split("\\.");
		if (parts.length >= 2) {
			return parts[0] + "-" + parts[1] + "-topics";
		} else {
			return "sonar-default-topics";
		}
	}
}