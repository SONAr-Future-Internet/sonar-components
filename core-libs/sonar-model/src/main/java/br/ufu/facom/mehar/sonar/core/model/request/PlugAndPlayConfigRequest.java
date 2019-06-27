package br.ufu.facom.mehar.sonar.core.model.request;

import java.util.Collection;

import br.ufu.facom.mehar.sonar.core.model.topology.Element;

public class PlugAndPlayConfigRequest {
	private Collection<Element> currentElementList;
	private Collection<Element> pluggedElementList;
	
	public Collection<Element> getCurrentElementList() {
		return currentElementList;
	}
	public void setCurrentElementList(Collection<Element> currentElementList) {
		this.currentElementList = currentElementList;
	}
	public Collection<Element> getPluggedElementList() {
		return pluggedElementList;
	}
	public void setPluggedElementList(Collection<Element> discoveredElements) {
		this.pluggedElementList = discoveredElements;
	}
}
