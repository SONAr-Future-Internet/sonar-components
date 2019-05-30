package br.ufu.facom.mehar.sonar.organizing.configuration.manager;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import br.ufu.facom.mehar.sonar.client.ndb.service.TopologyService;
import br.ufu.facom.mehar.sonar.client.nem.action.NetworkEventAction;
import br.ufu.facom.mehar.sonar.client.nem.configuration.SonarTopics;
import br.ufu.facom.mehar.sonar.client.nem.service.EventService;
import br.ufu.facom.mehar.sonar.client.nim.element.service.DeviceService;
import br.ufu.facom.mehar.sonar.core.model.configuration.Configuration;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;
import br.ufu.facom.mehar.sonar.core.model.topology.type.ElementState;
import br.ufu.facom.mehar.sonar.core.model.topology.type.ElementType;
import br.ufu.facom.mehar.sonar.core.util.ObjectUtils;
import br.ufu.facom.mehar.sonar.organizing.configuration.configuration.SCEConfiguration;
import br.ufu.facom.mehar.sonar.organizing.configuration.configuration.service.ControlConfigurationService;

@Component
public class TopologyConfigurationManager {
	private Logger logger = LoggerFactory.getLogger(TopologyConfigurationManager.class);

	@Autowired
	private SCEConfiguration configuration;
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private DeviceService deviceService; 
	
	@Autowired
	private TopologyService topologyService;
	
	@Autowired
	private ControlConfigurationService controlConfigurationService;
	
	@Autowired
	@Qualifier("taskScheduler")
    private TaskExecutor taskExecutor;
	
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
	
	@EventListener(ApplicationReadyEvent.class)
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
										deviceService.configureControllerIfSupported(element, configuration.getSDNSouthSeeds());
										
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
									}else {
										if(element.getOfDeviceId() != null) {
											List<Configuration> configuration = controlConfigurationService.getBasicDeviceConfiguration(element);
											if(configuration != null && !configuration.isEmpty()) {
												deviceService.configure(element, configuration);
											}
										}
									}
								}
								break;
							case(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_REFERRED_BY_CONTROLLER):
								if(ElementType.DEVICE.equals(element.getTypeElement())){
									//Query or Calculate configuration
									List<Configuration> configurationList = controlConfigurationService.getBasicDeviceConfiguration(element);
									
									//Apply configuration
									deviceService.configure(element, configurationList);
								}
								break;
							case(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_WAITING_CONFIGURATION):
								if(ElementType.DEVICE.equals(element.getTypeElement())){
									//Query or Calculate configuration
									List<Configuration> configurationList = controlConfigurationService.getConfigurationForDevice(element);
									
									//Apply configuration
									deviceService.configure(element, configurationList);
									
									//Update state
									element.setState(ElementState.CONFIGURED);
									Element updatedElement = topologyService.update(element);
									
									//Fire event of state changed
									eventService.publish(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_CONFIGURED, updatedElement);
									
								}
								break;
						}
					}
				});
			}
		});
	}
}
