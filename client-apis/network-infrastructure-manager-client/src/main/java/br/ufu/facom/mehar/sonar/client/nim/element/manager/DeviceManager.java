package br.ufu.facom.mehar.sonar.client.nim.element.manager;

import java.util.List;
import java.util.Set;

import br.ufu.facom.mehar.sonar.core.model.configuration.Flow;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;

public interface DeviceManager extends ElementManager {

	void configureController(Element element, String[] controllerTargets);

	void configureFlows(Element element, List<Flow> flows);

}
