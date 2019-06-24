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

import br.ufu.facom.mehar.sonar.client.ndb.service.ConfigurationDataService;
import br.ufu.facom.mehar.sonar.client.ndb.service.TopologyDataService;
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
import br.ufu.facom.mehar.sonar.organizing.configuration.algorithm.model.SimpleGraph;

@Service
public class ControlConfigurationService {
	
	private static final Logger logger = LoggerFactory.getLogger(ControlConfigurationService.class);

	@Autowired
	private ConfigurationDataService configurationService;

	@Autowired
	private TopologyDataService topologyService;
	
	public Graph<Element, Port> buildGraph(Set<Element> elementList) {
		Map<UUID, Port> mapPort = new HashMap<UUID, Port>();
		Map<UUID, Element> mapElement = new HashMap<UUID, Element>();
		for(Element element : elementList) {
			mapElement.put(element.getIdElement(), element);
			for(Port port : element.getPortList()) {
				mapPort.put(port.getIdPort(), port);
			}
		}

		Graph<Element, Port> graph = new Graph<Element, Port>();
		for(Element element : elementList) {
			for(Port port : element.getPortList()) {
				if(port.getRemoteIdPort() != null) {
					Port remotePort = mapPort.get(port.getRemoteIdPort());
					if(remotePort != null) {
						Element remoteElement = mapElement.get(remotePort.getIdElement());
						if(remoteElement != null) {
							graph.addLink(element, remoteElement, new Pair<Port, Port>(port, remotePort));
						}else {
							logger.error("Unknown remote element!");
						}
					}else {
						logger.error("Unknown remote port!");
					}
				}
			}
		}
		return graph;
	}
	
	public Set<Element> findServerRoots(Set<Element> elementSet) {
		Set<Element> servers = new HashSet<Element>();
		for(Element element : elementSet) {
			if(ElementType.SERVER.equals(element.getTypeElement())){
				servers.add(element);
			}
		}
		return servers;
	}
	
	public Path<Element, Port> calculateBestMultiPath(Element root, Graph<Element, Port> graph){
		return Dijkstra.calculateShortestPathFromSource(graph, root);
	}
	
	public SimpleGraph<Element> buildDependencyGraph(Path<Element, Port> multiPath) {
		SimpleGraph<Element> dependencyGraph = new SimpleGraph<Element>();
		for(Node<Element> node : multiPath.getPathMap().keySet()) {
			List<Pair<Node<Element>, Edge<Port>>> path = multiPath.getPath(node);
			Element previous = node.getValue();
			for(Pair<Node<Element>, Edge<Port>> segment : path) {
				Element current = segment.getFirst().getValue();
				if(!current.equals(multiPath.getOrigin().getValue())) {
					dependencyGraph.addLink(current,previous);
				}else {
					if(path.size() == 1) {
						dependencyGraph.addNode(node);
					}
				}
			}
		}
		
//		print(dependencyGraph);
		return dependencyGraph;
	}

	public Map<Element, List<Configuration>> generateConfiguration(Path<Element, Port> multiPath) {
		Map<Element, List<Configuration>> configurationMap = new HashMap<Element, List<Configuration>>();
		
		System.out.println();
		
		for(Node<Element> node : multiPath.getPathMap().keySet()) {
			List<Pair<Node<Element>, Edge<Port>>> path = multiPath.getPath(node);
			Element element = node.getValue();
//			Pair<Node<Element>, Edge<Port>> previousSegment = null;
			for(int i=0; i<path.size(); i++) {
				Pair<Node<Element>, Edge<Port>> segment = path.get(i);
				Element neighbor = segment.getFirst().getValue();
				if (!neighbor.equals(multiPath.getOrigin().getValue())) {
					Port portFromNeighborToElement = segment.getSecond().getPeerA();
					List<Configuration> configurationList = buildDeviceRouteConfiguration(neighbor, portFromNeighborToElement,element.getIpAddressList());
					if(configurationMap.containsKey(neighbor)) {
						configurationMap.get(neighbor).addAll(configurationList);
					}else {
						configurationMap.put(neighbor, configurationList);
					}
				}
				
				if(i == path.size() -1) {//last segment of path...
					Port portToRoot = segment.getSecond().getPeerB();
					List<Configuration> configurationList = buildDeviceRouteConfiguration(element, portToRoot, multiPath.getOrigin().getValue().getIpAddressList());
					configurationList.addAll(buildBasicDeviceConfiguration(element, portToRoot, Arrays.asList(multiPath.getOrigin().getValue())));
					if(configurationMap.containsKey(element)) {
						configurationMap.get(element).addAll(configurationList);
					}else {
						configurationMap.put(element, configurationList);
					}
				}
				
			}
		}
		
//		print(configurationMap);
//		
		validate(configurationMap);
		
		return configurationMap;
	}

	private void validate(Map<Element, List<Configuration>> configurationMap) {
		Map<UUID,Port> mapPort = new HashMap<UUID, Port>();
		Map<UUID,Element> mapDev = new HashMap<UUID, Element>();
		
		for(Element element : configurationMap.keySet()) {
			mapDev.put(element.getIdElement(), element);
			for(Port port : element.getPortList()) {
				mapPort.put(port.getIdPort(), port);
			}
		}
		
		for(Element element : configurationMap.keySet()) {
			for(Configuration configuration : configurationMap.get(element)) {
				if(configuration.getFlow() != null) {
					Flow flow = configuration.getFlow();
					for(FlowInstruction instruction : flow.getInstructions()) {
						if(instruction.getRefValue() != null) {
							Port port = mapPort.get(instruction.getRefValue());
							if(!port.getIdElement().equals(element.getIdElement())) {
								logger.error("Error!!! Flow with incorrect reference! "+element.getName()+" => "+configuration.getIdentification());
							}
						}
					}
				}
			}
		}
	}

	public SimpleGraph<Element> mergeGraph(SimpleGraph<Element> mergeToGraph, SimpleGraph<Element> mergeFromGraph) {
		return mergeToGraph.merge(mergeFromGraph);
	}
	
	public Map<Element, List<Configuration>> mergeConfiguration( Map<Element, List<Configuration>> mergeToConfiguration, Map<Element, List<Configuration>> mergeFromConfiguration) {
		for(Element element : mergeFromConfiguration.keySet()) {
			if(mergeToConfiguration.containsKey(element)) {
				Set<String> configurationIdentificationSet = new HashSet<String>();
				for(Configuration configuration : mergeToConfiguration.get(element)) {
					configurationIdentificationSet.add(configuration.getIdentification());
				}
				
				for(Configuration configuration : mergeFromConfiguration.get(element)) {
					if(!configurationIdentificationSet.contains(configuration.getIdentification())) {
						mergeToConfiguration.get(element).add(configuration);
					}
				}
			}else {
				mergeToConfiguration.put(element, mergeFromConfiguration.get(element));
			}
		}
		return mergeToConfiguration;
	}
	
	public SimpleGraph<Element> print(SimpleGraph<Element> dependencyGraph) {
		System.out.println("\nDependency Graph");
		for(Node<Element> node : dependencyGraph.getNodes()) {
			StringBuilder sb = new StringBuilder();
			sb.append(node.getValue().getName()+" => ");
			for(Node<Element> neighbor : dependencyGraph.getAdjacences(node)) {
				sb.append(neighbor.getValue().getName()+" ");
			}
			System.out.println(sb.toString());
		}
		System.out.println();
		
		return dependencyGraph;
	}
	
	public Map<Element, List<Configuration>> print(Map<Element, List<Configuration>> configurationMap) {
		for(Element element : configurationMap.keySet()) {
			StringBuilder sb = new StringBuilder();
			sb.append("["+element.getName()+"]\n");
			for(Configuration configuration : configurationMap.get(element)) {
				sb.append(configuration.getIdentification()+"\n");
			}
			System.out.println(sb.toString()+"\n");
		}
		
		return configurationMap;
	}

	public List<Configuration> getConfigurationForDevice(Element device){
		return this.getConfigurationForDevice(device, Boolean.FALSE);
	}
	
	public List<Configuration> getConfigurationForDevice(Element device, Boolean forceRecalculation){
		List<Configuration> controlConfiguration = calculateControlConfiguration(device, Boolean.FALSE);
		List<Configuration> deviceConfiguration = new ArrayList<Configuration>();
		for(Configuration configuration : controlConfiguration) {
			if(configuration.getIdElement().equals(device.getIdElement())) {
				deviceConfiguration.add(configuration);
			}
		}
		return deviceConfiguration;
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
											configuration.addAll(buildDeviceRouteConfiguration(element, portConnectedToServer, server.getIpAddressList()));
											if(!justRoutes) {
												configuration.addAll(buildBasicDeviceConfiguration(element, portConnectedToServer, serverList));
											}
										}else {
											logger.warn("Unable to use port to server. It doesn't have ofPortId."+portConnectedToServer);
										}
									} else {
										Port neighbordPortConnectedToElement = segment.getSecond().getPeerA();
										
										// Configure the neighbor to forward data to the device
										if(neighbordPortConnectedToElement.getOfPort() != null) {
											configuration.addAll(buildDeviceRouteConfiguration(neighbor, neighbordPortConnectedToElement,
												element.getIpAddressList()));
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

		return configuration;
	}
	
	private List<Configuration> buildDeviceRouteConfiguration(Element element, Port port, Set<String> managementIPAddressList) {
		List<Configuration> configurationList = new ArrayList<Configuration>();
		
		for(String nextHopIp : managementIPAddressList) {
			configurationList.add(buildFlowConfigurationTemplate(element,
				"Route "+element.getIpAddressList().iterator().next()+"->"+nextHopIp+" through "+port.getPortName(), 
				new Flow(element.getOfDeviceId(), element.getIdElement(),
					Arrays.asList(
							new FlowSelector(FlowSelectorType.ETH_TYPE, "0x0800"), 
							new FlowSelector(FlowSelectorType.IPV4_DST, nextHopIp+"/32")
						), 
						Arrays.asList(
							new FlowInstruction(FlowInstructionType.OUTPUT, port.getOfPort(), port.getIdPort())
						)
					)
				)
			);
		}
		
		return configurationList;
	}

	private List<Configuration> buildBasicDeviceConfiguration(Element element, Port portToServer, List<Element> serverList) {
		List<Configuration> configurationList = new ArrayList<Configuration>();
		
		// ARP Flow
		configurationList.add(buildFlowConfigurationTemplate(element, "ARP Flow",
				new Flow(element.getOfDeviceId(), element.getIdElement(),
						new FlowSelector(FlowSelectorType.ETH_TYPE, "0x0806"),
						new FlowInstruction(FlowInstructionType.NORMAL))));
		
		//LLDP
		configurationList.add(buildFlowConfigurationTemplate(element, "LLDP Flow",
				new Flow(element.getOfDeviceId(), element.getIdElement(),
						new FlowSelector(FlowSelectorType.ETH_TYPE, "0x88CC"),
						new FlowInstruction(FlowInstructionType.NORMAL))));
		
		//Local Processing
		for(String address : element.getIpAddressList()) {
			configurationList.add(buildFlowConfigurationTemplate(element, "Local Processing",
				new Flow(element.getOfDeviceId(), element.getIdElement(),
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
		configurationList.add(buildFlowConfigurationTemplate(element, "DHCP Controller " + portToServer.getPortName(),
				new Flow(element.getOfDeviceId(), element.getIdElement(),
					Arrays.asList(
							new FlowSelector(FlowSelectorType.ETH_TYPE, "0x0800"), 
							new FlowSelector(FlowSelectorType.IPV4_DST, "255.255.255.255/32"),
							new FlowSelector(FlowSelectorType.IP_PROTO, "17"),
							new FlowSelector(FlowSelectorType.UDP_DST, "67") 
					), 
					Arrays.asList(
							new FlowInstruction(FlowInstructionType.CONTROLLER)
					)
				)
			));

		//DHCP Routes
//		configurationList.add(buildFlowConfigurationTemplate(element, "DHCP Broadcast (->) " + portToServer.getPortName(),
//			new Flow(element.getOfDeviceId(), element.getIdElement(),
//				Arrays.asList(
//						new FlowSelector(FlowSelectorType.ETH_TYPE, "0x0800"), 
//						new FlowSelector(FlowSelectorType.IPV4_DST, "255.255.255.255/32"),
//						new FlowSelector(FlowSelectorType.IP_PROTO, "17"),
//						new FlowSelector(FlowSelectorType.UDP_DST, "67") 
//				), 
//				Arrays.asList(
//					new FlowInstruction(FlowInstructionType.OUTPUT, portToServer.getOfPort(), portToServer.getIdPort())
//				)
//			)
//		));
//			
//		for(Element server : serverList) {
//			for(String ipServer : server.getIpAddressList()) {
//				configurationList.add(buildFlowConfigurationTemplate(element, "DHCP Broadcast (<-)", //TODO Loop Avoidance 
//					new Flow(element.getOfDeviceId(), element.getIdElement(),
//						Arrays.asList(
//								new FlowSelector(FlowSelectorType.ETH_TYPE, "0x0800"), 
//								new FlowSelector(FlowSelectorType.IPV4_SRC, ipServer+"/32"),
//								new FlowSelector(FlowSelectorType.IPV4_DST, "192.168.0.255/32"),
//								new FlowSelector(FlowSelectorType.IP_PROTO, "17"),
//								new FlowSelector(FlowSelectorType.UDP_DST, "68") 
//						), 
//						Arrays.asList(
//							new FlowInstruction(FlowInstructionType.FLOOD)
//						)
//					)
//				));
//			}
//		}
		
		return configurationList;
	}
	
	
	private Configuration buildFlowConfigurationTemplate(Element element, String label, Flow flow) {
		Configuration configuration = new Configuration();
		configuration.setIdElement(element.getIdElement());
		configuration.setElement(element);
		configuration.setType(ConfigurationType.FLOW_CREATION);
		configuration.setIdentification(label + " [" + element.getIpAddressList().iterator().next() + "]");
		configuration.setFlow(flow);
		return configuration;
	}
	
	public static void main(String[] args) {
		Flow flow = new Flow("of:00001a5a2332f14b",
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
		flow.setDeviceId("of:0000ea6e631ba540");
		
		System.out.println(ObjectUtils.fromObject(ElementModelTranslator.convertToONOSFlow(Arrays.asList(flow))));
	}
	
//	public List<Configuration> getGenericRouteConfiguration(Element element){
//		return new ArrayList<Configuration>(Arrays.asList(
//			buildFlowConfigurationTemplate(element,
//				"Route "+element.getIpAddressList().iterator().next()+"-> any host on 192.168.0.0/24 with learning-switch", 
//				new Flow(element.getOfDeviceId(), element.getIdElement(),
//					Arrays.asList(
//							new FlowSelector(FlowSelectorType.ETH_TYPE, "0x0800"), 
//							new FlowSelector(FlowSelectorType.IPV4_DST, "192.168.0.0/24")
//						), 
//						Arrays.asList(
//							new FlowInstruction(FlowInstructionType.NORMAL)
//						)
//					)
//				)
//			)
//		);
//	}
}
