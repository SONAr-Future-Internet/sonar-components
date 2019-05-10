package br.ufu.facom.mehar.sonar.client.nim.element.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow.ONOSFlow;
import br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow.ONOSFlowGroup;
import br.ufu.facom.mehar.sonar.core.model.configuration.Flow;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;

public class ElementModelTranslator {

	public static ONOSFlowGroup generateONOSFlowGroup(Element element, Set<Flow> flows, Boolean permanent) {
		ONOSFlowGroup flowGroup = new ONOSFlowGroup();
		List<ONOSFlow> onosFlows = new ArrayList<ONOSFlow>();
		for(Flow flow : flows) {
			onosFlows.add(generateONOSFlow(element, flow, permanent));
		}
		
		flowGroup.setFlows(onosFlows);
		
		return flowGroup;
	}

	private static ONOSFlow generateONOSFlow(Element element, Flow flow, Boolean permanent) {
		ONOSFlow onosFlow = new ONOSFlow();
		onosFlow.setIsPermanent(permanent);
		
//		onosFlow.setDeviceId(deviceId);
		return onosFlow;
	}

}
