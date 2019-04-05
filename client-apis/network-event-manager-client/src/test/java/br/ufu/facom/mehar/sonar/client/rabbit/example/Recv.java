package br.ufu.facom.mehar.sonar.client.rabbit.example;
import java.io.IOException;

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;

public class Recv {

    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        
      //prevent processing multiple messages at same time
//   	 int prefetchCount = 1;
//   	 channel.basicQos(prefetchCount);


        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        
        boolean autoAck = false;

        channel.basicConsume(QUEUE_NAME, autoAck, new DeliverCallback() {
			@Override
			public void handle(String consumerTag, Delivery delivery) throws IOException {
				String message = new String(delivery.getBody(), "UTF-8");
	            System.out.print(" [x] Received '" + message + "'");
	            try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}finally {
					System.out.println(" -> done!");
					channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
				}
	            
			}
		}, new CancelCallback() {
			@Override
			public void handle(String arg0) throws IOException {
			}
		});
    }
}