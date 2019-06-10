package br.ufu.facom.mehar.sonar.client.nim.element.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow.ONOSDevice;
import br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow.ONOSFlow;
import br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow.ONOSFlowGroup;
import br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow.ONOSFlowSelector;
import br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow.ONOSFlowSelectorCriteria;
import br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow.ONOSFlowTreatment;
import br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow.ONOSFlowTreatmentInstruction;
import br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow.ONOSPort;
import br.ufu.facom.mehar.sonar.core.model.configuration.Flow;
import br.ufu.facom.mehar.sonar.core.model.configuration.FlowInstruction;
import br.ufu.facom.mehar.sonar.core.model.configuration.FlowSelector;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;
import br.ufu.facom.mehar.sonar.core.model.topology.type.ElementType;
import br.ufu.facom.mehar.sonar.core.model.topology.type.PortState;
import br.ufu.facom.mehar.sonar.core.util.IPUtils;

public class ElementModelTranslator {

	public static ONOSFlowGroup convertToONOSFlow(List<Flow> flows) {
		ONOSFlowGroup flowGroup = new ONOSFlowGroup();
		List<ONOSFlow> onosFlows = new ArrayList<ONOSFlow>();
		for (Flow flow : flows) {
			onosFlows.add(convertToONOSFlow(flow));
		}

		flowGroup.setFlows(onosFlows);

		return flowGroup;
	}

	private static ONOSFlow convertToONOSFlow(Flow flow) {
		ONOSFlow onosFlow = new ONOSFlow();
		onosFlow.setIsPermanent(flow.getIsPermanent());
		onosFlow.setPriority(flow.getPriority());
		onosFlow.setTimeout(flow.getTimeout());
		onosFlow.setDeviceId(flow.getDeviceId());
		if(flow.getSelectors() != null && !flow.getSelectors().isEmpty()) {
			onosFlow.setSelector(new ONOSFlowSelector());
			onosFlow.getSelector().setCriteria(new ArrayList<ONOSFlowSelectorCriteria>());
			for(FlowSelector selector : flow.getSelectors()) {
				onosFlow.getSelector().getCriteria().add(convertToONOSFlowSelectorCriteria(selector));
			}
		}
		if(flow.getInstructions() != null && !flow.getInstructions().isEmpty()) {
			onosFlow.setTreatment(new ONOSFlowTreatment());
			onosFlow.getTreatment().setInstructions(new ArrayList<ONOSFlowTreatmentInstruction>());
			for(FlowInstruction instruction : flow.getInstructions()) {
				onosFlow.getTreatment().getInstructions().add(convertToONOSFlowTreatmentInstruction(instruction));
			}
		}
		
		return onosFlow;
	}

	private static ONOSFlowTreatmentInstruction convertToONOSFlowTreatmentInstruction(FlowInstruction instruction) {
		ONOSFlowTreatmentInstruction treatment = new ONOSFlowTreatmentInstruction();
		switch (instruction.getType()) {
			case FLOOD:
				treatment.setType("OUTPUT");
				treatment.setPort("FLOOD");
				break;
			case GROUP:
				treatment.setType("GROUP");
				treatment.setGroupId(Integer.parseInt(instruction.getValue()));
				break;
			case METER:
				treatment.setType("METER");
				treatment.setMeterId(Integer.parseInt(instruction.getValue()));
				break;
			case NORMAL:
				treatment.setType("OUTPUT");
				treatment.setPort("NORMAL");
				break;
			case OUTPUT:
				treatment.setType("OUTPUT");
				treatment.setPort(instruction.getValue());
				break;
			case QUEUE:
				treatment.setType("QUEUE");
				String[] parts = instruction.getValue().split(",", 2);
				treatment.setPort(Integer.parseInt(parts[0]));
				treatment.setQueueId(Integer.parseInt(parts[1]));
				break;
			case TABLE:
				treatment.setType("TABLE");
				treatment.setTableId(Integer.parseInt(instruction.getValue()));
				break;
			case CONTROLLER:
				treatment.setType("OUTPUT");
				treatment.setPort("CONTROLLER");
		
		}
		
		return treatment;
	}

	private static ONOSFlowSelectorCriteria convertToONOSFlowSelectorCriteria(FlowSelector selector) {
		ONOSFlowSelectorCriteria criteria = new ONOSFlowSelectorCriteria();
		switch(selector.getType()) {
			case CHANNEL_SPACING: 
				criteria.setType("CHANNEL_SPACING");
				criteria.setChannelSpacing(Integer.parseInt(selector.getValue())); 
				break;
			case ETH_DST:
				criteria.setType("ETH_DST");
				criteria.setMac(selector.getValue()); 
				break;
			case ETH_SRC:
				criteria.setType("ETH_SRC");
				criteria.setMac(selector.getValue()); 
				break;
			case ETH_TYPE:
				criteria.setType("ETH_TYPE");
				criteria.setEthType(selector.getValue()); 
				break;
			case GRID_TYPE:
				criteria.setType("GRID_TYPE");
				criteria.setGridType(selector.getValue()); 
				break;
			case ICMPV4_CODE:
				criteria.setType("ICMPV4_CODE");
				criteria.setIcmpCode(Integer.parseInt(selector.getValue()));   
				break;
			case ICMPV4_TYPE:
				criteria.setType("ICMPV4_TYPE");
				criteria.setIcmpType(selector.getValue()); 
				break;
			case ICMPV6_CODE:
				criteria.setType("ICMPV6_CODE");
				criteria.setIcmpv6Code(Integer.parseInt(selector.getValue()));  
				break;
			case ICMPV6_TYPE:
				criteria.setType("ICMPV6_TYPE");
				criteria.setIcmpv6Type(Integer.parseInt(selector.getValue()));  
				break;
			case IN_PHY_PORT:
				criteria.setType("IN_PHY_PORT");
				criteria.setPort(selector.getValue()); 
				break;
			case IN_PORT:
				criteria.setType("IN_PORT");
				criteria.setPort(selector.getValue()); 
				break;
			case INNER_VLAN_PCP:
				criteria.setType("INNER_VLAN_PCP");
				criteria.setInnerPriority(selector.getValue()); 
				break;
			case INNER_VLAN_VID:
				criteria.setType("INNER_VLAN_VID");
				criteria.setInnerVlanId(selector.getValue()); 
				break;
			case IP_DSCP:
				criteria.setType("IP_DSCP");
				criteria.setIpDscp(Integer.parseInt(selector.getValue())); 
				break;
			case IP_ECN:
				criteria.setType("IP_ECN");
				criteria.setIpEcn(Integer.parseInt(selector.getValue())); 
				break;
			case IP_PROTO:
				criteria.setType("IP_PROTO");
				criteria.setProtocol(Integer.parseInt(selector.getValue()));  
				break;
			case IPV4_DST:
				criteria.setType("IPV4_DST");
				criteria.setIp(selector.getValue().contains("/")? selector.getValue() : selector.getValue()+"/32"); 
				break;
			case IPV4_SRC:
				criteria.setType("IPV4_SRC");
				criteria.setIp(selector.getValue().contains("/")? selector.getValue() : selector.getValue()+"/32"); 
				break;
			case IPV6_DST:
				criteria.setType("IPV6_DST");
				criteria.setIp(selector.getValue()); 
				break;
			case IPV6_EXTHDR:
				criteria.setType("IPV6_EXTHDR");
				criteria.setExthdrFlags(Integer.parseInt(selector.getValue())); 
				break;
			case IPV6_FLABEL:
				criteria.setType("IPV6_FLABEL");
				criteria.setFlowlabel(Integer.parseInt(selector.getValue())); 
				break;
			case IPV6_ND_SLL:
				criteria.setType("IPV6_ND_SLL");
				criteria.setMac(selector.getValue()); 
				break;
			case IPV6_ND_TARGET:
				criteria.setType("IPV6_ND_TARGET");
				criteria.setTargetAddress(selector.getValue()); 
				break;
			case IPV6_ND_TLL:
				criteria.setType("IPV6_ND_TLL");
				criteria.setMac(selector.getValue()); 
				break;
			case IPV6_SRC:
				criteria.setType("IPV6_SRC");
				criteria.setIp(selector.getValue()); 
				break;
			case METADATA:
				criteria.setType("METADATA");
				criteria.setMetadata(selector.getValue()); 
				break;
			case MPLS_LABEL:
				criteria.setType("MPLS_LABEL");
				criteria.setLabel(Integer.parseInt(selector.getValue())); 
				break;
			case OCH_SIGID:
				criteria.setType("OCH_SIGID");
				criteria.setOchSignalId(Integer.parseInt(selector.getValue())); 
				break;
			case OCH_SIGTYPE:
				criteria.setType("OCH_SIGTYPE");
				criteria.setOchSignalType(Integer.parseInt(selector.getValue())); 
				break;
			case ODU_SIGID:
				criteria.setType("ODU_SIGID");
				criteria.setOduSignalId(Integer.parseInt(selector.getValue())); 
				break;
			case ODU_SIGTYPE:
				criteria.setType("ODU_SIGTYPE");
				criteria.setOduSignalType(Integer.parseInt(selector.getValue())); 
				break;
			case SCTP_DST:
				criteria.setType("SCTP_DST");
				criteria.setSctpPort(Integer.parseInt(selector.getValue())); 
				break;
			case SCTP_SRC:
				criteria.setType("SCTP_SRC");
				criteria.setSctpPort(Integer.parseInt(selector.getValue())); 
				break;
			case SLOT_GRANULARITY:
				criteria.setType("SLOT_GRANULARITY");
				criteria.setSlotGranularity(Integer.parseInt(selector.getValue())); 
				break;
			case SPACING_MULIPLIER:
				criteria.setType("SPACING_MULIPLIER");
				criteria.setSpacingMultiplier(Integer.parseInt(selector.getValue())); 
				break;
			case TCP_DST:
				criteria.setType("TCP_DST");
				criteria.setTcpPort(Integer.parseInt(selector.getValue())); 
				break;
			case TCP_SRC:
				criteria.setType("TCP_SRC");
				criteria.setTcpPort(Integer.parseInt(selector.getValue())); 
				break;
			case TUNNEL_ID:
				criteria.setType("TUNNEL_ID");
				criteria.setTunnelId(Integer.parseInt(selector.getValue())); 
				break;
			case UDP_DST:
				criteria.setType("UDP_DST");
				criteria.setUdpPort(Integer.parseInt(selector.getValue())); 
				break;
			case UDP_SRC:
				criteria.setType("UDP_SRC");
				criteria.setUdpPort(Integer.parseInt(selector.getValue())); 
				break;
			case VLAN_PCP:
				criteria.setType("VLAN_PCP");
				criteria.setPriority(selector.getValue()); 
				break;
			case VLAN_VID:
				criteria.setType("VLAN_VID");
				criteria.setVlanId(selector.getValue()); 
				break;
			
		}
		return criteria;
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
