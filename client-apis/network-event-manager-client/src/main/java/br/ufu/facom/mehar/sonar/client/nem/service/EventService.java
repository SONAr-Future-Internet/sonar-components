package br.ufu.facom.mehar.sonar.client.nem.service;

import br.ufu.facom.mehar.sonar.client.nem.action.NetworkEventAction;
import br.ufu.facom.mehar.sonar.core.util.ObjectUtils;

public abstract class EventService {
	public void publish(String topic, Object object) {
		this.publish(topic, ObjectUtils.fromObject(object));
	}
	
	public abstract void publish(String topic, String json);
	
	public abstract void subscribe(String topic, NetworkEventAction action);
}
