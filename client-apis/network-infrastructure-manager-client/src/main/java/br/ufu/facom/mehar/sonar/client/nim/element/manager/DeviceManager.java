package br.ufu.facom.mehar.sonar.client.nim.element.manager;

import java.util.Set;

import br.ufu.facom.mehar.sonar.core.model.configuration.Flow;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;

public interface DeviceManager extends ElementManager {

	void configureController(Element element, String[] controllerTargets);

	void configureFlows(Element element, Set<Flow> flows, Boolean permanent);

}
