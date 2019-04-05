package br.ufu.facom.mehar.sonar.client.rabbit.example;

import java.util.Date;

import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class Send {
    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
    	 ConnectionFactory factory = new ConnectionFactory();
         factory.setHost("localhost");
         try (Connection connection = factory.newConnection();
              Channel channel = connection.createChannel()) {
        	 
        	 boolean durable = false;
        	 BasicProperties prop = MessageProperties.BASIC;
        	 
        	 String exchange = ""; //default
        	 
             channel.queueDeclare(QUEUE_NAME, durable, false, false, null);
             String message = new Date().toString();
             channel.basicPublish(exchange, QUEUE_NAME, null, message.getBytes("UTF-8"));
             System.out.println(" [x] Sent '" + message + "'");
         }
    }
}
