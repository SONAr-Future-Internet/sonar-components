package br.ufu.facom.mehar.sonar.client.nem.service;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;

import br.ufu.facom.mehar.sonar.client.nem.action.NetworkEventAction;
import br.ufu.facom.mehar.sonar.client.nem.configuration.NEMConfiguration;
import br.ufu.facom.mehar.sonar.client.nem.exception.PublishErrorException;
import br.ufu.facom.mehar.sonar.core.util.ObjectUtils;

public abstract class EventService {
	
	@Autowired
	protected NEMConfiguration configuration;
	
	public void publish(String topic, Object object) {
		this.publish(topic, ObjectUtils.fromObject(object));
	}
	
	public void publish(String topic, String json) {
		try {
			this.publish(topic, json.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new PublishErrorException(e);
		}
	}
	
	public abstract void publish(String topic, byte[] payload);
	
	public abstract void subscribe(String topic, NetworkEventAction action);
}
