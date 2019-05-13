package br.ufu.facom.mehar.sonar.client.nim.element.manager;

import java.util.Collection;

import br.ufu.facom.mehar.sonar.core.model.topology.Element;

public interface SDNManager extends DeviceManager {

	Collection<Element> discover();

}
