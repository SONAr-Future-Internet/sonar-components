package br.ufu.facom.mehar.sonar.client.rabbit.example;
import java.io.IOException;

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;

public class ReceiveLogsTopic {

  private static final String EXCHANGE_NAME = "topic_logs";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(EXCHANGE_NAME, "topic");
    String queueName = channel.queueDeclare().getQueue();

//All
//    for (String bindingKey : new String[] {"#"}) {
//        channel.queueBind(queueName, EXCHANGE_NAME, bindingKey);
//    }
    
//Critical
//    for (String bindingKey : new String[] {"*.critical"}) {
//        channel.queueBind(queueName, EXCHANGE_NAME, bindingKey);
//    }
    
//Kern and Critical
    for (String bindingKey : new String[] {"kern.*","*.critical"}) {
        channel.queueBind(queueName, EXCHANGE_NAME, bindingKey);
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