package br.ufu.facom.mehar.sonar.organizing.configuration.configuration.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.client.ndb.service.ConfigurationService;
import br.ufu.facom.mehar.sonar.client.ndb.service.TopologyService;
import br.ufu.facom.mehar.sonar.client.nim.element.model.ElementModelTranslator;
import br.ufu.facom.mehar.sonar.core.model.configuration.Configuration;
import br.ufu.facom.mehar.sonar.core.model.configuration.ConfigurationType;
import br.ufu.facom.mehar.sonar.core.model.configuration.Flow;
import br.ufu.facom.mehar.sonar.core.model.configuration.FlowInstruction;
import br.ufu.facom.mehar.sonar.core.model.configuration.FlowInstructionType;
import br.ufu.facom.mehar.sonar.core.model.configuration.FlowSelector;
import br.ufu.facom.mehar.sonar.core.model.configuration.FlowSelectorType;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;
import br.ufu.facom.mehar.sonar.core.model.topology.type.ElementState;
import br.ufu.facom.mehar.sonar.core.model.topology.type.ElementType;
import br.ufu.facom.mehar.sonar.core.util.ObjectUtils;
import br.ufu.facom.mehar.sonar.core.util.Pair;
import br.ufu.facom.mehar.sonar.organizing.configuration.algorithm.Dijkstra;
import br.ufu.facom.mehar.sonar.organizing.configuration.algorithm.model.Edge;
import br.ufu.facom.mehar.sonar.organizing.configuration.algorithm.model.Graph;
import br.ufu.facom.mehar.sonar.organizing.configuration.algorithm.model.Node;
import br.ufu.facom.mehar.sonar.organizing.configuration.algorithm.model.Path;

@Service
public class ControlConfigurationService {
	
	private static final Logger logger = LoggerFactory.getLogger(ControlConfigurationService.class);

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private TopologyService topologyService;

	public List<Configuration> getConfigurationForDevice(Element device){
		return this.getConfigurationForDevice(device, Boolean.FALSE);
	}
	
	public List<Configuration> getConfigurationForDevice(Element device, Boolean forceRecalculation){
		//Look for configuration pre-computed
//		if(!forceRecalculation) {
//			List<Configuration> deviceConfiguration = configurationService
//						.getControlConfigurationByIdElement(device.getIdElement());
//				if (!deviceConfiguration.isEmpty() && !hasScenarioChangedSinceConfiguration(deviceConfiguration)) {
//					return deviceConfiguration;
//				}
//		}
		
		//Calculate new configuration
		List<Configuration> controlConfiguration = calculateControlConfiguration(device, Boolean.FALSE);
//		configurationService.saveOrReplaceControlConfiguration(controlConfiguration);
		List<Configuration> deviceConfiguration = new ArrayList<Configuration>();
		for(Configuration configuration : controlConfiguration) {
			if(configuration.getIdElement().equals(device.getIdElement())) {
				deviceConfiguration.add(configuration);
			}
		}
		return deviceConfiguration;
	}
	
	public List<Configuration> getBasicDeviceConfiguration(Element element) {
		List<Configuration> configurationList = getConfigurationForDevice(element);
		if(configurationList != null) {
			return configurationList;
		}
		
		List<Element> serverList = new ArrayList<Element>(); 
		for (Element e : topologyService.getElements()) {
			if (ElementType.SERVER.equals(e)) {
				serverList.add(element);
			}
		}
		
		//Basic Device Configuration
		configurationList = buildBasicDeviceConfiguration(element, serverList);
		
		return configurationList;
	}
	
	public List<Configuration> getGenericRouteConfiguration(Element element){
		return new ArrayList<Configuration>(Arrays.asList(
			buildFlowConfigurationTemplate(element,
				"Route "+element.getManagementIPAddressList().iterator().next()+"-> any host on 192.168.0.0/24 with learning-switch", 
				new Flow(
					Arrays.asList(
							new FlowSelector(FlowSelectorType.ETH_TYPE, "0x0800"), 
							new FlowSelector(FlowSelectorType.IPV4_DST, "192.168.0.0/24")
						), 
						Arrays.asList(
							new FlowInstruction(FlowInstructionType.NORMAL)
						)
					)
				)
			)
		);
	}
	

	public Map<Element, List<Configuration>> getRouteConfigurationToAccessAnElement(Element element) {
		//Calculate new configuration
		List<Configuration> controlConfiguration = calculateControlConfiguration(element, Boolean.TRUE);
		Map<Element, List<Configuration>> deviceConfigurationMap = new HashMap<Element, List<Configuration>>();
		for(Configuration configuration : controlConfiguration) {
			if(deviceConfigurationMap.containsKey(configuration.getElement())) {
				deviceConfigurationMap.get(configuration.getElement()).add(configuration);
			}else {
				deviceConfigurationMap.put(configuration.getElement(),new ArrayList<Configuration>(Arrays.asList(configuration)));
			}
		}
		return deviceConfigurationMap;
	}
	
	
	public List<Configuration> calculateControlConfiguration(Element elementTarget, Boolean justRoutes) {
		//result
		List<Configuration> configuration = new ArrayList<Configuration>();

		/**
		 * Memory Cache
		 */
		Map<UUID, Element> idElementToElementMap = new HashMap<UUID, Element>();
		Map<UUID, Port> idPortToPort = new HashMap<UUID, Port>();

		/**
		 * Query Data
		 */
		List<Element> serverList = new ArrayList<Element>();
		List<Element> deviceList = new ArrayList<Element>();
		for (Element element : topologyService.getElements()) {
			if (ElementType.SERVER.equals(element.getTypeElement())) {
				serverList.add(element);
				idElementToElementMap.put(element.getIdElement(), element);
			}
			if (ElementType.DEVICE.equals(element.getTypeElement())) {
				if(element.equals(elementTarget) ||  element.getState().afterOrEqual(ElementState.DISCOVERED)) {
					deviceList.add(element);
					idElementToElementMap.put(element.getIdElement(), element);
				}
			}
		}
		Set<Port> linkedPortList = topologyService.getLinkedPortsByIdElement(idElementToElementMap.keySet());
		for (Port linkedPort : linkedPortList) {
			idPortToPort.put(linkedPort.getIdPort(), linkedPort);
		}

		/**
		 * Calculate shortest paths and generate configuration
		 */
		for (Element server : serverList) {
			Graph<Element, Port> graph = new Graph<Element, Port>();
			for (Port peerPortA : linkedPortList) {
				Port peerPortB = idPortToPort.get(peerPortA.getRemoteIdPort());
				if(peerPortB == null) {
					peerPortB = topologyService.getPortById(peerPortA.getRemoteIdPort());
				}
				if(peerPortB != null) {
					Element peerA = idElementToElementMap.get(peerPortA.getIdElement());
					Element peerB = idElementToElementMap.get(peerPortB.getIdElement());
					if(peerA != null && peerB != null) {
						if (server.equals(peerA) || server.equals(peerB) || 
							(	ElementType.DEVICE.equals(peerA.getTypeElement()) && 
								ElementType.DEVICE.equals(peerB.getTypeElement()) &&
								( peerA.getState().equals(ElementState.CONFIGURED) || (elementTarget != null &&  peerA.equals(elementTarget))) &&
								( peerB.getState().equals(ElementState.CONFIGURED) || (elementTarget != null && peerB.equals(elementTarget)))
							)
						) {
		
							graph.addLink(peerA, peerB, new Pair<Port,Port>(peerPortA, peerPortB));
						}
					}
				}else {
					logger.warn("PeerPort Remote not found in database! Port:"+peerPortA);
				}
			}
			Node<Element> serverNode = graph.getNodeByValue(server);
			if(serverNode != null) {
				Map<Node<Element>, Edge<Port>> serverMapAdjacences = graph.getMapAdjacences().get(serverNode);
				if(serverMapAdjacences != null && !serverMapAdjacences.isEmpty()) {
					Path<Element,Port> multiPath = Dijkstra.calculateShortestPathFromSource(graph, server);
					for (Node<Element> node : multiPath.getPathMap().keySet()) {
						Element element = node.getValue();
						if(elementTarget == null || elementTarget.getIdElement().equals(element.getIdElement())) {
							if (!element.equals(server)) {
								List<Pair<Node<Element>, Edge<Port>>> path = multiPath.getPath(node);
								for(Pair<Node<Element>, Edge<Port>> segment : path) {
									Element neighbor = segment.getFirst().getValue();
									if (neighbor.equals(server)) {
										Port portConnectedToServer = segment.getSecond().getPeerB();
										// COnfigure route to server 
										if(portConnectedToServer.getOfPort() != null) {
											configuration.addAll(buildDeviceRouteConfiguration(element, portConnectedToServer, server.getManagementIPAddressList()));
										}else {
											logger.warn("Unable to use port to server. It doesn't have ofPortId."+portConnectedToServer);
										}
									} else {
										Port neighbordPortConnectedToElement = segment.getSecond().getPeerA();
										
										// Configure the neighbor to forward data to the device
										if(neighbordPortConnectedToElement.getOfPort() != null) {
											configuration.addAll(buildDeviceRouteConfiguration(neighbor, neighbordPortConnectedToElement,
												element.getManagementIPAddressList()));
										}else {
											logger.warn("Unable to use port to create path to the server. It doesn't have ofPortId."+neighbordPortConnectedToElement);
										}
									}
								}
							}
						}
					}
				}
			}
		}

		/**
		 * Generate basic configuration for each device
		 */
		if(elementTarget == null) {
			for (Element element : deviceList) {
				if(element.getOfDeviceId() != null) {
					configuration.addAll(buildBasicDeviceConfiguration(element, serverList));
				}
			}
		}else {
			if(elementTarget.getOfDeviceId() != null && !justRoutes) {
				configuration.addAll(buildBasicDeviceConfiguration(elementTarget, serverList));
			}
		}
		
		return configuration;
	}
	
	private List<Configuration> buildDeviceRouteConfiguration(Element element, Port port, Set<String> managementIPAddressList) {
		List<Configuration> configurationList = new ArrayList<Configuration>();
		
		for(String nextHopIp : managementIPAddressList) {
			configurationList.add(buildFlowConfigurationTemplate(element,
				"Route "+element.getManagementIPAddressList().iterator().next()+"->"+nextHopIp+" through "+port.getPortName(), 
				new Flow(
					Arrays.asList(
							new FlowSelector(FlowSelectorType.ETH_TYPE, "0x0800"), 
							new FlowSelector(FlowSelectorType.IPV4_DST, nextHopIp+"/32")
						), 
						Arrays.asList(
							new FlowInstruction(FlowInstructionType.OUTPUT, port.getOfPort() != null? port.getOfPort(): port.getPortName())
						)
					)
				)
			);
		}
		
		return configurationList;
	}

	private List<Configuration> buildBasicDeviceConfiguration(Element element, List<Element> serverList) {
		List<Configuration> configurationList = new ArrayList<Configuration>();
		
		// ARP Flow
		configurationList.add(buildFlowConfigurationTemplate(element, "ARP Flow",
				new Flow(new FlowSelector(FlowSelectorType.ETH_TYPE, "0x0806"),
						new FlowInstruction(FlowInstructionType.NORMAL))));
		
		//LLDP
		configurationList.add(buildFlowConfigurationTemplate(element, "LLDP Flow",
				new Flow(new FlowSelector(FlowSelectorType.ETH_TYPE, "0x88CC"),
						new FlowInstruction(FlowInstructionType.NORMAL))));
		
		//Local Processing
		for(String address : element.getManagementIPAddressList()) {
			configurationList.add(buildFlowConfigurationTemplate(element, "Local Processing",
				new Flow(
					Arrays.asList(
						new FlowSelector(FlowSelectorType.ETH_TYPE, "0x0800"), 
						new FlowSelector(FlowSelectorType.IPV4_DST, address+"/32")
					), 
					Arrays.asList(
						new FlowInstruction(FlowInstructionType.NORMAL)
					)
				)
			));
		}
		
		//DHCP Routes
		configurationList.add(buildFlowConfigurationTemplate(element, "DHCP Broadcast (->)",
			new Flow(
				Arrays.asList(
						new FlowSelector(FlowSelectorType.ETH_TYPE, "0x0800"), 
						new FlowSelector(FlowSelectorType.IPV4_DST, "255.255.255.255/32"),
						new FlowSelector(FlowSelectorType.IP_PROTO, "17"),
						new FlowSelector(FlowSelectorType.UDP_DST, "67") 
				), 
				Arrays.asList(
					new FlowInstruction(FlowInstructionType.OUTPUT, "1")
				)
			)
		));
			
		for(Element server : serverList) {
			for(String ipServer : server.getManagementIPAddressList()) {
				configurationList.add(buildFlowConfigurationTemplate(element, "DHCP Broadcast (<-)", //TODO Loop Avoidance 
					new Flow(
						Arrays.asList(
								new FlowSelector(FlowSelectorType.ETH_TYPE, "0x0800"), 
								new FlowSelector(FlowSelectorType.IPV4_SRC, ipServer+"/32"),
								new FlowSelector(FlowSelectorType.IPV4_DST, "192.168.0.255/32"),
								new FlowSelector(FlowSelectorType.IP_PROTO, "17"),
								new FlowSelector(FlowSelectorType.UDP_DST, "68") 
						), 
						Arrays.asList(
							new FlowInstruction(FlowInstructionType.FLOOD)
						)
					)
				));
			}
		}
		
		return configurationList;
	}
	
	
	private Configuration buildFlowConfigurationTemplate(Element element, String label, Flow flow) {
		Configuration configuration = new Configuration();
		configuration.setIdElement(element.getIdElement());
		configuration.setElement(element);
		configuration.setType(ConfigurationType.FLOW_CREATION);
		configuration.setIdentification(label + " [" + element.getManagementIPAddressList().iterator().next() + "]");
		configuration.setFlow(flow);
		return configuration;
	}
	
	public static void main(String[] args) {
		Flow flow = new Flow(
				Arrays.asList(
						new FlowSelector(FlowSelectorType.ETH_TYPE, "0x0800"), 
						new FlowSelector(FlowSelectorType.IPV4_DST, "255.255.255.255/32"),
						new FlowSelector(FlowSelectorType.IP_PROTO, "17"),
						new FlowSelector(FlowSelectorType.UDP_DST, "67") 
				), 
				Arrays.asList(
					new FlowInstruction(FlowInstructionType.CONTROLLER)
				)
			);
		
		Element element = new Element();
		element.setTypeElement(ElementType.DEVICE);
		element.setManagementIPAddressList(new HashSet<String>(Arrays.asList("192.168.0.2")));
		element.setOfDeviceId("of:0000eeb72f191b4f");
		
		System.out.println(ObjectUtils.fromObject(ElementModelTranslator.convertToONOSFlow(element, Arrays.asList(flow))));
	}
}
