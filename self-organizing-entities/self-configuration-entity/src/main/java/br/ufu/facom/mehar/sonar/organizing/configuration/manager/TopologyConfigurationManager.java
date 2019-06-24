package br.ufu.facom.mehar.sonar.organizing.configuration.manager;

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
import br.ufu.facom.mehar.sonar.core.model.topology.Domain;
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
	
	@EventListener(ApplicationReadyEvent.class)
	public void boot() {
		System.out.println("Listening "+SonarTopics.TOPIC_SCE_CALL_BOOT);
		eventService.subscribe(SonarTopics.TOPIC_SCE_CALL_BOOT, new NetworkEventAction() {
			@Override
			public void handle(String event, String json) {
				if(!runningBoot) {
					try {
						runningBoot = true;
						Domain bootDomain = ObjectUtils.toObject(json, Domain.class);
						
						//Stage 2 - Routing Calculation
						eventService.publish(SonarTopics.TOPIC_BOOT_START_ROUTING_STAGE, "");
						//Build Graph
						Graph<Element, Port> graph = controlConfigurationService.buildGraph(bootDomain.getElementList());
						
						//Prepare Data 'Holders'
						SimpleGraph<Element> generalDependencyGraph = new SimpleGraph<Element>();
						Map<Element, List<Configuration>> generalConfigurationMap = new HashMap<Element, List<Configuration>>();
						
						//Calculate 'Paths', 'Dependencies' and 'Configurations'
						Element rootWithController = null;
						for(Element root : controlConfigurationService.findServerRoots(bootDomain.getElementList())) {
							rootWithController = root;
							Path<Element, Port> multiPath = controlConfigurationService.calculateBestMultiPath(root, graph);
							SimpleGraph<Element> dependencyGraph = controlConfigurationService.buildDependencyGraph(multiPath);
							Map<Element, List<Configuration>> configurationMap = controlConfigurationService.generateConfiguration(multiPath);
							generalDependencyGraph = controlConfigurationService.mergeGraph(generalDependencyGraph, dependencyGraph);
							generalConfigurationMap = controlConfigurationService.mergeConfiguration(generalConfigurationMap, configurationMap);
						}
						eventService.publish(SonarTopics.TOPIC_BOOT_FINISH_ROUTING_STAGE, "");
						
						//Stage 3 - Configuring
						eventService.publish(SonarTopics.TOPIC_BOOT_START_CONFIGURATION_STAGE, "");
						Set<Element> configuredElements = new HashSet<Element>();
						while(!generalDependencyGraph.isEmpty()) {
							Set<Element> leafs = generalDependencyGraph.removeLeafs();
							if(!leafs.isEmpty()) {
								Controller controller = getControllerByServer(rootWithController);
								
								if(controller != null) {
									//Configure Controller
									deviceService.configureController(leafs, controller , Boolean.TRUE);
									
									//Configure Flows
									Map<Element, List<Configuration>> subConfigurationMap = new HashMap<Element, List<Configuration>>();
									for(Element element : leafs) {
										if(generalConfigurationMap.containsKey(element)) {
											subConfigurationMap.put(element, generalConfigurationMap.get(element));
										}
									}
									deviceService.configure(controller, subConfigurationMap, Boolean.TRUE);
									configuredElements.addAll(leafs);
									for(Element element : leafs) {
										element.setState(ElementState.CONFIGURED);
									}
								}
							}
						}
						eventService.publish(SonarTopics.TOPIC_BOOT_FINISH_CONFIGURATION_STAGE, "");
						
						//Ending...
						eventService.publish(SonarTopics.TOPIC_SCE_CALLBACK_BOOT, bootDomain);
					}finally {
						runningBoot = false;
					}
				}
			}
		});
	}
	
	@EventListener(ApplicationReadyEvent.class)
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
	}
	
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
}
