package br.ufu.facom.mehar.sonar.client.rabbit.example;
import java.io.IOException;

import com.rabbitmq.client.*;

public class ReceiveLogsDirect {

  private static final String EXCHANGE_NAME = "direct_logs";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(EXCHANGE_NAME, "direct");
    String queueName = channel.queueDeclare().getQueue();

    for (String severity : new String[] {"warn"}) {
        channel.queueBind(queueName, EXCHANGE_NAME, severity);
    }
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    channel.basicConsume(queueName, true, new DeliverCallback() {
		@Override
		public void handle(String consumerTag, Delivery delivery) throws IOException {
			String message = new String(delivery.getBody(), "UTF-8");
	        System.out.println(" [x] Received '" +
	            delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
		}
	}, new CancelCallback() {
		@Override
		public void handle(String arg0) throws IOException {
		}
	});
  }
}