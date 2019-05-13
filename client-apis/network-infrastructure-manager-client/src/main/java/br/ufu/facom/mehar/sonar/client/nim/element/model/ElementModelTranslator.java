package br.ufu.facom.mehar.sonar.client.nim.element.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow.ONOSDevice;
import br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow.ONOSFlow;
import br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow.ONOSFlowGroup;
import br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow.ONOSPort;
import br.ufu.facom.mehar.sonar.core.model.configuration.Flow;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;
import br.ufu.facom.mehar.sonar.core.model.topology.type.ElementType;
import br.ufu.facom.mehar.sonar.core.model.topology.type.PortState;
import br.ufu.facom.mehar.sonar.core.util.IPUtils;

public class ElementModelTranslator {

	public static ONOSFlowGroup convertToONOSFlow(Element element, Set<Flow> flows, Boolean permanent) {
		ONOSFlowGroup flowGroup = new ONOSFlowGroup();
		List<ONOSFlow> onosFlows = new ArrayList<ONOSFlow>();
		for (Flow flow : flows) {
			onosFlows.add(convertToONOSFlow(element, flow, permanent));
		}

		flowGroup.setFlows(onosFlows);

		return flowGroup;
	}

	private static ONOSFlow convertToONOSFlow(Element element, Flow flow, Boolean permanent) {
		ONOSFlow onosFlow = new ONOSFlow();
		onosFlow.setIsPermanent(permanent);

//		onosFlow.setDeviceId(deviceId);
		return onosFlow;
	}

	public static Set<Element> convertToElement(Set<ONOSDevice> onosDeviceSet) {
		Set<Element> resultSet = new HashSet<Element>();
		for (ONOSDevice onosDevice : onosDeviceSet) {
			resultSet.add(convertToElement(onosDevice));
		}
		return resultSet;
	}

	public static Element convertToElement(ONOSDevice onosDevice) {
		Element element = new Element();
		element.setOfDeviceId(onosDevice.getId());
		element.setManufacturer(onosDevice.getMfr());
		element.setProduct(onosDevice.getHw());
		element.setSoftware(onosDevice.getDriver() != null ? onosDevice.getDriver() + " " + onosDevice.getSw()
				: onosDevice.getSw());
		element.setPortList(new HashSet<Port>());
		if ("SWITCH".equalsIgnoreCase(onosDevice.getType())) {
			element.setTypeElement(ElementType.DEVICE);
		}
		if (onosDevice.getAnnotations().containsKey("managementAddress")) {
			element.setManagementIPAddressList(
					new HashSet<String>(Arrays.asList(onosDevice.getAnnotations().get("managementAddress"))));
		}

		for (ONOSPort onosPort : onosDevice.getPorts()) {
			Port port = new Port();
			port.setOfPort(onosPort.getPort());
			port.setSpeed(onosPort.getPortSpeed());
			port.setState(onosPort.getIsEnabled() ? PortState.UP : PortState.DOWN);
			if (onosPort.getAnnotations().containsKey("adminState")) {
				port.setAdminState(
						"enabled".equalsIgnoreCase(onosPort.getAnnotations().get("adminState")) ? PortState.UP
								: PortState.DOWN);
			}
			if (onosPort.getAnnotations().containsKey("portMac")) {
				port.setMacAddress(IPUtils.normalizeMAC(onosPort.getAnnotations().get("portMac")));
			}
			if (onosPort.getAnnotations().containsKey("portName")) {
				port.setPortName(onosPort.getAnnotations().get("portName"));
			}

			element.getPortList().add(port);
		}
		return element;
	}

}
