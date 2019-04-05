package br.ufu.facom.mehar.sonar.client.nem.action;

public abstract class NetworkEventAction {
	public abstract void handle(String event, String json);
}
