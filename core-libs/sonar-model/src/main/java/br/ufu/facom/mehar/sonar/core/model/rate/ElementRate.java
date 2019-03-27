package br.ufu.facom.mehar.sonar.core.model.rate;

import br.ufu.facom.mehar.sonar.core.model.service.Policy;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;

public class ElementRate {
	private Port port;
	private Policy policy;
	private Long rate;

	public Port getPort() {
		return port;
	}

	public void setPort(Port port) {
		this.port = port;
	}

	public Policy getPolicy() {
		return policy;
	}

	public void setPolicy(Policy policy) {
		this.policy = policy;
	}

	public Long getRate() {
		return rate;
	}

	public void setRate(Long rate) {
		this.rate = rate;
	}

}
