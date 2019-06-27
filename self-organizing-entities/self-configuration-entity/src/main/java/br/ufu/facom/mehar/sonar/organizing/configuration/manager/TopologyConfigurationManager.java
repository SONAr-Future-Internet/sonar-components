package br.ufu.facom.mehar.sonar.organizing.configuration.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import br.ufu.facom.mehar.sonar.client.ndb.service.CoreDataService;
import br.ufu.facom.mehar.sonar.client.ndb.service.TopologyDataService;
import br.ufu.facom.mehar.sonar.client.nem.action.NetworkEventAction;
import br.ufu.facom.mehar.sonar.client.nem.configuration.SonarTopics;
import br.ufu.facom.mehar.sonar.client.nem.service.EventService;
import br.ufu.facom.mehar.sonar.client.nim.element.service.DeviceService;
import br.ufu.facom.mehar.sonar.core.model.configuration.Configuration;
import br.ufu.facom.mehar.sonar.core.model.core.Controller;
import br.ufu.facom.mehar.sonar.core.model.request.BootConfigurationReply;
import br.ufu.facom.mehar.sonar.core.model.request.BootConfigurationRequest;
import br.ufu.facom.mehar.sonar.core.model.request.PlugAndPlayConfigReply;
import br.ufu.facom.mehar.sonar.core.model.request.PlugAndPlayConfigRequest;
import br.ufu.facom.mehar.sonar.core.model.request.PlugAndPlayRouteReply;
import br.ufu.facom.mehar.sonar.core.model.request.PlugAndPlayRouteRequest;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;
import br.ufu.facom.mehar.sonar.core.model.topology.type.ElementState;
import br.ufu.facom.mehar.sonar.core.util.ObjectUtils;
import br.ufu.facom.mehar.sonar.organizing.configuration.algorithm.model.Graph;
import br.ufu.facom.mehar.sonar.organizing.configuration.algorithm.model.Path;
import br.ufu.facom.mehar.sonar.organizing.configuration.algorithm.model.SimpleGraph;
import br.ufu.facom.mehar.sonar.organizing.configuration.configuration.service.ControlConfigurationService;

@Component
public class TopologyConfigurationManager {
	private Logger logger = LoggerFactory.getLogger(TopologyConfigurationManager.class);

	@Autowired
	private EventService eventService;
	
	@Autowired
	private DeviceService deviceService; 
	
	@Autowired
	private TopologyDataService topologyService;
	
	@Autowired
	private CoreDataService coreService;
	
	@Autowired
	private ControlConfigurationService controlConfigurationService;
	
	@Autowired
	@Qualifier("taskScheduler")
    private TaskExecutor taskExecutor;
	
	//State control variables
	private volatile Set<UUID> devicesInConfigurationProcess = Collections.synchronizedSet(new HashSet<UUID>());
	private static volatile boolean runningBoot = false;
	/**
	 * Bootstrapping
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void listenToBootRequest() {
		logger.info("Listening "+SonarTopics.TOPIC_SCE_CALL_BOOT);
		eventService.subscribe(SonarTopics.TOPIC_SCE_CALL_BOOT, new NetworkEventAction() {
			@Override
			public void handle(String event, String json) {
				if(!runningBoot) {
					try {
						runningBoot = true;
						BootConfigurationRequest bootRequest = ObjectUtils.toObject(json, BootConfigurationRequest.class);
						
						/*
						 * Stage 2 - Routing Calculation
						 */
						eventService.publish(SonarTopics.TOPIC_BOOT_START_ROUTING_STAGE, "");
						//Build Graph
						Graph<Element, Port> graph = controlConfigurationService.buildGraph(bootRequest.getElementList());
						
						//Prepare Data 'Holders'
						SimpleGraph<Element> generalDependencyGraph = new SimpleGraph<Element>();
						Map<Element, List<Configuration>> generalConfigurationMap = new HashMap<Element, List<Configuration>>();
						
						//Calculate 'Paths', 'Dependencies' and 'Configurations'
						Element rootWithController = null;
						for(Element root : controlConfigurationService.findServerRoots(bootRequest.getElementList())) {
							rootWithController = root;
							Path<Element, Port> multiPath = controlConfigurationService.calculateBestMultiPath(root, graph);
							SimpleGraph<Element> dependencyGraph = controlConfigurationService.buildDependencyGraph(multiPath);
							Map<Element, List<Configuration>> configurationMap = controlConfigurationService.generateConfiguration(multiPath);
							generalDependencyGraph = controlConfigurationService.mergeGraph(generalDependencyGraph, dependencyGraph);
							generalConfigurationMap = controlConfigurationService.mergeConfiguration(generalConfigurationMap, configurationMap);
						}
						eventService.publish(SonarTopics.TOPIC_BOOT_FINISH_ROUTING_STAGE, "");
						
						/*
						 * Stage 3 - Configuring
						 */
						Controller controller = getControllerByServer(rootWithController);
						eventService.publish(SonarTopics.TOPIC_BOOT_START_CONFIGURATION_STAGE, "");
						if(controller != null) {
							while(!generalDependencyGraph.isEmpty()) {
								Set<Element> leafs = generalDependencyGraph.removeLeafs();
								if(!leafs.isEmpty()) {
									//Configure Controller
									deviceService.configureController(leafs, controller , Boolean.TRUE);
									
									//Filter Flows
									Map<Element, List<Configuration>> subConfigurationMap = filterConfiguration(generalConfigurationMap, leafs);
									
									//Configure Flows
									deviceService.configure(controller, subConfigurationMap, Boolean.TRUE);
									
									//Set state
									for(Element element : leafs) {
										element.setState(ElementState.CONFIGURED);
									}
								}
							}
						}else {
							logger.error("Unable to find a controller in server "+rootWithController);
						}
						eventService.publish(SonarTopics.TOPIC_BOOT_FINISH_CONFIGURATION_STAGE, "");
						
						//Ending...
						BootConfigurationReply bootReply = new BootConfigurationReply();
						bootReply.setElementList(bootRequest.getElementList());
						eventService.publish(SonarTopics.TOPIC_SCE_CALLBACK_BOOT, bootReply);
					}finally {
						runningBoot = false;
					}
				}
			}
		});
	}
	
	/**
	 * Plug and Play
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void listenToPlugAndPlayEvents() {
		logger.info("Listening "+SonarTopics.TOPIC_SCE_CALL_PNP_CONFIG);
		eventService.subscribe(SonarTopics.TOPIC_SCE_CALL_PNP_CONFIG, new NetworkEventAction() {
			@Override
			public void handle(String event, String json) {
				logger.info("Configuring new plugged elements! "+json);
				
				//Request data
				PlugAndPlayConfigRequest configRequest = ObjectUtils.toObject(json, PlugAndPlayConfigRequest.class);
				
				try {
					//Choose Controller
					Controller choosedController = null;
					Set<Element> roots = controlConfigurationService.findServerRoots(configRequest.getCurrentElementList());
					for(Element root : roots ) {
						choosedController = getControllerByServer(root);
						if(choosedController != null) {
							break;
						}
					}
					
					//If the Controller is correctly defined
					if(choosedController != null) {
						List<Port> pluggedPortList = getPluggedPorts(configRequest.getPluggedElementList());
						List<Port> remotePluggedPortList = getRemotePluggedPorts(pluggedPortList, configRequest.getCurrentElementList());
						
						// Simple case : one new element connected by using an unique port
						if(configRequest.getPluggedElementList().size() == 1 && pluggedPortList.size() == 1) {
							Element pluggedElement = configRequest.getPluggedElementList().iterator().next();
							Port pluggedPort = pluggedPortList.iterator().next();
							
							//Generate Configuration
							Map<Element, List<Configuration>> configurationMap = controlConfigurationService.generateConfigurationBasicAndRouteToServers(pluggedElement, pluggedPort, roots);
							
							//Configure Controller
							deviceService.configureController(configRequest.getPluggedElementList(), choosedController , Boolean.TRUE);
							
							//Deploy Configuration
							deviceService.configure(choosedController, configurationMap, Boolean.TRUE);
							
							//Set State
							for(Element element : configRequest.getPluggedElementList()) {
								element.setState(ElementState.CONFIGURED);
							}
							
							//Send Reply
							PlugAndPlayConfigReply configReply = new PlugAndPlayConfigReply();
							configReply.setConfiguredElementList(configRequest.getPluggedElementList());
							configReply.setChangedPortList(remotePluggedPortList);
							eventService.publish(SonarTopics.TOPIC_SCE_CALLBACK_PNP_CONFIG, configReply);
							return;
						}
						
						// Complex case : multiple elements or multiple links connected
						//Merge Elements
						Set<Element> mergedElementList = new HashSet<Element>();
						mergedElementList.addAll(configRequest.getCurrentElementList());
						mergedElementList.addAll(configRequest.getPluggedElementList());
						
						//Build Graph
						Graph<Element, Port> graph = controlConfigurationService.buildGraph(mergedElementList);
	
						//Prepare Data 'Holders'
						Map<Element, List<Configuration>> generalConfigurationMap = new HashMap<Element, List<Configuration>>();
						SimpleGraph<Element> generalDependencyGraph = new SimpleGraph<Element>();
	
						//Calculate 'Paths', 'Dependencies' and 'Configurations'
						for(Element root : controlConfigurationService.findServerRoots(mergedElementList)) {
							Path<Element, Port> multiPath = controlConfigurationService.calculateBestMultiPath(root, graph);
							SimpleGraph<Element> dependencyGraph = controlConfigurationService.buildDependencyGraph(multiPath);
							Map<Element, List<Configuration>> configurationMap = controlConfigurationService.generateConfigurationRelatedToSpecificElements(multiPath, configRequest.getPluggedElementList());
							generalDependencyGraph = controlConfigurationService.mergeGraph(generalDependencyGraph, dependencyGraph);
							generalConfigurationMap = controlConfigurationService.mergeConfiguration(generalConfigurationMap, configurationMap);
						}
						
						//Split configuration per Controller, Device and Kind
						Map<Controller,Map<Element,List<Configuration>>> configurationForCurrentDevicesPerControllerMap = new HashMap<Controller, Map<Element,List<Configuration>>>();
						Map<UUID, Controller> controllerMap = new HashMap<UUID, Controller>();
						for(Controller controller : coreService.getControllers()) {
							controllerMap.put(controller.getIdController(), controller);
						}
						for(Element element : generalConfigurationMap.keySet()) {
							for(UUID idController : element.getOfControllerList()) {
								if(controllerMap.containsKey(idController)) {
									Controller controller = controllerMap.get(idController);
									if(!configRequest.getPluggedElementList().contains(element)) {
										if(!configurationForCurrentDevicesPerControllerMap.containsKey(controller)) {
											configurationForCurrentDevicesPerControllerMap.put(controller, new HashMap<Element, List<Configuration>>());
										}
										if(!configurationForCurrentDevicesPerControllerMap.get(controller).containsKey(element)) {
											configurationForCurrentDevicesPerControllerMap.get(controller).put(element, new ArrayList<Configuration>());
										}
										configurationForCurrentDevicesPerControllerMap.get(controller).get(element).addAll(generalConfigurationMap.get(element));
									}
								}
							}
						}
	
						//Configure all current elements and remove already created elements from dependency graph
						for(Controller controller : configurationForCurrentDevicesPerControllerMap.keySet()) {
							deviceService.configure(controller,configurationForCurrentDevicesPerControllerMap.get(controller) , Boolean.TRUE);
							
						}
						generalDependencyGraph.removeAll(configRequest.getCurrentElementList());
						
						//Configure plugged elements
						while(!generalDependencyGraph.isEmpty()) {
							Set<Element> leafs = generalDependencyGraph.removeLeafs();
							if(!leafs.isEmpty()) {
								//Configure Controller
								deviceService.configureController(leafs, choosedController , Boolean.TRUE);
								
								//Filter Flows
								Map<Element, List<Configuration>> subConfigurationMap = filterConfiguration(generalConfigurationMap, leafs);
								
								//Configure Flows
								deviceService.configure(choosedController, subConfigurationMap, Boolean.TRUE);
								
								//Set State
								for(Element element : leafs) {
									element.setState(ElementState.CONFIGURED);
								}
							}
						}
						
						//Send Reply
						PlugAndPlayConfigReply configReply = new PlugAndPlayConfigReply();
						configReply.setConfiguredElementList(configRequest.getPluggedElementList());
						configReply.setChangedPortList(remotePluggedPortList);
						eventService.publish(SonarTopics.TOPIC_SCE_CALLBACK_PNP_CONFIG, configReply);
					}else {
						logger.error("Error while detecting/choosing a controller for plug-and-play.");
						PlugAndPlayConfigReply configReply = new PlugAndPlayConfigReply();
						eventService.publish(SonarTopics.TOPIC_SCE_CALLBACK_PNP_CONFIG, configReply);
					}
				} catch (Exception e) {
					logger.error("Error while configuring devices in plug-and-play task.",e);
					PlugAndPlayConfigReply configReply = new PlugAndPlayConfigReply();
					eventService.publish(SonarTopics.TOPIC_SCE_CALLBACK_PNP_CONFIG, configReply);
				}
			}
		});
		
		logger.info("Listening "+SonarTopics.TOPIC_SCE_CALL_PNP_ROUTE);
		eventService.subscribe(SonarTopics.TOPIC_SCE_CALL_PNP_ROUTE, new NetworkEventAction() {
			@Override
			public void handle(String event, String json) {
				logger.info("Configuring routes for new plugged elements! "+json);
				//Request data
				PlugAndPlayRouteRequest routeRequest = ObjectUtils.toObject(json, PlugAndPlayRouteRequest.class);
				
				try {
					String targetIp = routeRequest.getAssignedIP();
					Element attachmentElement = routeRequest.getAttachmentElement();
					Port attachmentPort = routeRequest.getAttachmentPort();
					
					//Current Topology
					List<Element> elementList = queryCurrentTopology();
					
					//Build Graph
					Graph<Element, Port> graph = controlConfigurationService.buildGraph(elementList);
					
					//Prepare Data 'Holders'
					Map<Element, List<Configuration>> generalConfigurationMap = new HashMap<Element, List<Configuration>>();
					
					//Calculate 'Paths', 'Dependencies' and 'Configurations'
					for(Element root : controlConfigurationService.findServerRoots(elementList)) {
						Path<Element, Port> multiPath = controlConfigurationService.calculateBestMultiPath(root, graph);
						Map<Element, List<Configuration>> configurationMap = controlConfigurationService.generateConfigurationAccessRoute(multiPath, attachmentElement, attachmentPort, targetIp);
						generalConfigurationMap = controlConfigurationService.mergeConfiguration(generalConfigurationMap, configurationMap);
					}
					
					//Split configuration per Controller
					Map<UUID, Controller> controllerMap = new HashMap<UUID, Controller>();
					for(Controller controller : coreService.getControllers()) {
						controllerMap.put(controller.getIdController(), controller);
					}
					Map<Controller,Map<Element,List<Configuration>>> configurationPerControllerMap = new HashMap<Controller, Map<Element,List<Configuration>>>();
					for(Element element : generalConfigurationMap.keySet()) {
						for(UUID idController : element.getOfControllerList()) {
							if(controllerMap.containsKey(idController)) {
								Controller controller = controllerMap.get(idController);
								if(!configurationPerControllerMap.containsKey(controller)) {
									configurationPerControllerMap.put(controller, new HashMap<Element, List<Configuration>>());
								}
								if(!configurationPerControllerMap.get(controller).containsKey(element)) {
									configurationPerControllerMap.get(controller).put(element, new ArrayList<Configuration>());
								}
								configurationPerControllerMap.get(controller).get(element).addAll(generalConfigurationMap.get(element));
							}
						}
					}
					
					//Configure Routes
					for(Controller controller : configurationPerControllerMap.keySet()) {
						deviceService.configure(controller, configurationPerControllerMap.get(controller), Boolean.TRUE);
					}
				} finally {
					//Send Reply (the finally avoids deadlock)
					PlugAndPlayRouteReply routeReply = new PlugAndPlayRouteReply();
					routeReply.setAssignedIP(routeRequest.getAssignedIP());
					routeReply.setMacAddress(routeRequest.getMacAddress());
					eventService.publish(SonarTopics.TOPIC_SCE_CALLBACK_PNP_ROUTE, routeReply);
				}
			}
		});
	}
	
	/**
	 * Util
	 */
	private Controller getControllerByServer(Element rootWithController) {
		for(Controller controller : coreService.getControllers()) {
			String south = controller.getSouth();
			if(south != null && !south.isEmpty() && south.contains(":")) {
				String ip = south.split(":",2)[0];
				if(rootWithController.getIpAddressList().contains(ip)) {
					return controller;
				}
			}
		}
		return null;
	}
	
	private Map<Element, List<Configuration>> filterConfiguration(Map<Element, List<Configuration>> generalConfigurationMap, Set<Element> leafs) {
		Map<Element, List<Configuration>> subConfigurationMap = new HashMap<Element, List<Configuration>>();
		for(Element element : leafs) {
			if(generalConfigurationMap.containsKey(element)) {
				subConfigurationMap.put(element, generalConfigurationMap.get(element));
			}
		}
		return subConfigurationMap;
	}

	private List<Port> getRemotePluggedPorts(List<Port> pluggedPortList, Collection<Element> currentElementList) {
		Map<UUID, Port> portMap = new HashMap<UUID, Port>();
		for(Element element : currentElementList) {
			for(Port port : element.getPortList()) {
				portMap.put(port.getIdPort(), port);
			}
		}
		
		List<Port> remotePluggedPortList = new ArrayList<Port>();
		for(Port port : pluggedPortList) {
			if(port.getRemoteIdPort() != null) {
				Port remotePort = portMap.get(port.getRemoteIdPort());
				if(remotePort != null) {
					remotePluggedPortList.add(remotePort);
				}
			}
		}
		
		return remotePluggedPortList;
	}

	private List<Port> getPluggedPorts(Collection<Element> pluggedElementList) {
		List<Port> pluggedPortList = new ArrayList<Port>();
		for(Element element : pluggedElementList) {
			for(Port port : element.getPortList()) {
				if(port.getRemoteIdPort() != null) {
					pluggedPortList.add(port);
				}
			}
		}
		return pluggedPortList;
	}
	
	private List<Element> queryCurrentTopology() {
		List<Element> elementList = topologyService.getElements();
		Set<Port> portList = topologyService.getPorts();
		Map<UUID,Element> elementMap = new HashMap<UUID, Element>();
		for(Element element : elementList) {
			elementMap.put(element.getIdElement(), element);
		}
		for(Port port : portList) {
			Element element = elementMap.get(port.getIdElement());
			if(element != null) {
				if(element.getPortList() == null) {
					element.setPortList(new HashSet<Port>(Arrays.asList(port)));
				}else {
					element.getPortList().add(port);
				}
			}
		}
		return elementList;
	}
	
	/*@EventListener(ApplicationReadyEvent.class)
	public void listenToTopologyLinkUpdateEvents() {
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				logger.info("Listening to '"+SonarTopics.TOPIC_TOPOLOGY_LINKS_CHANGED+"'...");
				eventService.subscribe(SonarTopics.TOPIC_TOPOLOGY_LINKS_CHANGED, new NetworkEventAction() {
					@Override
					public void handle(String event, String json) {
						System.out.println("Event: "+event+" JSON:"+json);
					}
				});
			}
		});
	}*/
	
	/*@EventListener(ApplicationReadyEvent.class)
	public void listenToElementStateChanged() {
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				logger.info("Listening to '"+SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED+"'...");
				eventService.subscribe(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE, new NetworkEventAction() {
					@Override
					public void handle(String event, String json) {
						System.out.println("Event: "+event+" JSON:"+json);
						Element element = ObjectUtils.toObject(json, Element.class);
						switch(event) {
							case(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_DISCOVERED):
							case(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_WAITING_CONTROLLER_ASSIGNMENT):
								try {
									if(ElementType.DEVICE.equals(element.getTypeElement())){
										//Configure controller (if supported)
										deviceService.configureController(element, configuration.getSDNSouthSeeds());
										
										//Update state to 'Configured'
										element.setState(ElementState.WAITING_CONTROLLER_CONNECTION);
										Element updatedElement = topologyService.update(element);
										
										//Fire event of state changed
										eventService.publish(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_WAITING_CONTROLLER_CONNECTION, updatedElement);
									}
								} catch(Exception e) {
									logger.error("Error while configuring controllers on element: "+ObjectUtils.toString(element));
								}
								break;
							case(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_CONNECTED_TO_TOPOLOGY):
							case(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_WAITING_ROUTES):
								if(ElementType.DEVICE.equals(element.getTypeElement())){
									//Query or Calculate configuration
									Map<Element, List<Configuration>> configurationList = controlConfigurationService.getRouteConfigurationToAccessAnElement(element);
									if(configurationList != null && !configurationList.isEmpty()) {
										for(Element elementToConfigure : configurationList.keySet()) {
											//Apply configuration
											deviceService.configure(elementToConfigure, configurationList.get(elementToConfigure));
										}
										
										//Update state to 'Configured'
										element.setState(ElementState.WAITING_DISCOVERY);
										Element updatedElement = topologyService.update(element);
										
										//Fire event of state changed
										eventService.publish(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_WAITING_DISCOVERY, updatedElement);
									}
								}
								break;
//							case(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_REFERRED_BY_CONTROLLER):
//								if(ElementType.DEVICE.equals(element.getTypeElement())){
//									//Query or Calculate configuration
//									List<Configuration> configurationList = controlConfigurationService.getBasicDeviceConfiguration(element);
//									
//									//Apply configuration
//									deviceService.configure(element, configurationList);
//								}
//								break;
							case(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_WAITING_CONFIGURATION):
								if(ElementType.DEVICE.equals(element.getTypeElement())){
									if(!devicesInConfigurationProcess.contains(element.getIdElement())) {
										devicesInConfigurationProcess.add(element.getIdElement());
										try {
											//Query or Calculate configuration
											List<Configuration> configurationList = controlConfigurationService.getConfigurationForDevice(element);
											
											//Apply configuration
											deviceService.configure(element, configurationList);
											
											//Update state
											element.setState(ElementState.CONFIGURED);
											Element updatedElement = topologyService.update(element);
											
											//Fire event of state changed
											eventService.publish(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_CONFIGURED, updatedElement);
										}finally {
											devicesInConfigurationProcess.remove(element.getIdElement());
										}
									}
								}
								break;
						}
					}
				});
			}
		});
	}*/
}
