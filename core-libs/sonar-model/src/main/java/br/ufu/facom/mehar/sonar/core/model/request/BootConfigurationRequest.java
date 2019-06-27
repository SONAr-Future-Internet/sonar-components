package br.ufu.facom.mehar.sonar.core.model.request;

import java.util.Set;

import br.ufu.facom.mehar.sonar.core.model.topology.Element;

public class BootConfigurationRequest {
	private Set<Element> elementList;

	public Set<Element> getElementList() {
		return elementList;
	}

	public void setElementList(Set<Element> elementList) {
		this.elementList = elementList;
	}
}
