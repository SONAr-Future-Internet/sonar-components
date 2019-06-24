package br.ufu.facom.mehar.sonar.client.nim.element.manager;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import br.ufu.facom.mehar.sonar.core.model.configuration.Flow;
import br.ufu.facom.mehar.sonar.core.model.core.Controller;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;

public interface SDNManager {

	Collection<Element> discover(Controller controller);

	Set<String> configureFlows(Controller controller, List<Flow> flows, Boolean waitFlowCreation);

	Set<String> configureFlows(Controller controller, Element element, List<Flow> flows);

}
