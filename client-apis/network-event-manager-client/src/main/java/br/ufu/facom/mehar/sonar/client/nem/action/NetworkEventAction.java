package br.ufu.facom.mehar.sonar.client.nem.action;

import java.io.UnsupportedEncodingException;

import br.ufu.facom.mehar.sonar.client.nem.exception.SubscribeErrorException;

public abstract class NetworkEventAction {
	public abstract void handle(String event, String json);
	
	public void handle(String event, byte[] payload) {
		try {
			this.handle(event, new String(payload, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new SubscribeErrorException(e);
		}
	}
}
