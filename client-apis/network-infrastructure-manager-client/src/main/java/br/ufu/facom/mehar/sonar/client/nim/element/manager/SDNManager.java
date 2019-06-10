package br.ufu.facom.mehar.sonar.client.nim.element.manager;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import br.ufu.facom.mehar.sonar.core.model.configuration.Flow;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;

public interface SDNManager extends DeviceManager {

	Collection<Element> discover();

	Set<String> configureFlows(List<Flow> flows, Boolean waitFlowCreation);

}
