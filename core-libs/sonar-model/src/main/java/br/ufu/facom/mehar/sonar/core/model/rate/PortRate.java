package br.ufu.facom.mehar.sonar.core.model.rate;

import br.ufu.facom.mehar.sonar.core.model.service.Policy;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;

public class PortRate {
	private Element element;
	private Policy policy;
	private Long rate;

	public Element getElement() {
		return element;
	}

	public void setElement(Element element) {
		this.element = element;
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
