package br.ufu.facom.mehar.sonar.client.nim.element.manager;

import java.util.Set;

import org.springframework.stereotype.Component;

import br.ufu.facom.mehar.sonar.client.nim.element.exception.MethodNotImplementedYetException;
import br.ufu.facom.mehar.sonar.client.nim.element.exception.MethodNotSupportedException;
import br.ufu.facom.mehar.sonar.client.nim.element.model.ElementModelTranslator;
import br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow.ONOSFlowGroup;
import br.ufu.facom.mehar.sonar.core.model.configuration.Flow;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;

@Component("onos")
public class ONOSManager implements DeviceManager{

	@Override
	public Element discover(String ip) {
		throw new MethodNotImplementedYetException("Method 'discover' of ONOSManager not implemented yet!");
	}

	@Override
	public void configureController(Element element, String[] controllerTargets) {
		throw new MethodNotSupportedException("Method 'configureController' of ONOSManager not suported!");
	}
	
	@Override
	public void configureFlows(Element element, Set<Flow> flows, Boolean permanent) {
		ONOSFlowGroup group = ElementModelTranslator.generateONOSFlowGroup(element, flows, permanent);
	}

}
