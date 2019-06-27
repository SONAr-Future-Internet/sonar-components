package br.ufu.facom.mehar.sonar.core.model.request;

import java.util.Collection;

import br.ufu.facom.mehar.sonar.core.model.topology.Element;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;

public class PlugAndPlayConfigReply {
	private Collection<Element> configuredElementList;
	private Collection<Port> changedPortList;

	public Collection<Element> getConfiguredElementList() {
		return configuredElementList;
	}

	public void setConfiguredElementList(Collection<Element> collection) {
		this.configuredElementList = collection;
	}

	public Collection<Port> getChangedPortList() {
		return changedPortList;
	}

	public void setChangedPortList(Collection<Port> changedPortList) {
		this.changedPortList = changedPortList;
	}

}
