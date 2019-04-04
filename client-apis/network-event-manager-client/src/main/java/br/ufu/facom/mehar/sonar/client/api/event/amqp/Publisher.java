package br.ufu.facom.mehar.sonar.client.api.event.amqp;

public interface Publisher {

	public void publish(Object message);
}
