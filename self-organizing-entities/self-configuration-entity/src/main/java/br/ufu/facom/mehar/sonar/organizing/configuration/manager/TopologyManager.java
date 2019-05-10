package br.ufu.facom.mehar.sonar.organizing.configuration.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import br.ufu.facom.mehar.sonar.client.nem.action.NetworkEventAction;
import br.ufu.facom.mehar.sonar.client.nem.configuration.SonarTopics;
import br.ufu.facom.mehar.sonar.client.nem.service.EventService;
import br.ufu.facom.mehar.sonar.client.nim.element.service.DeviceService;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;
import br.ufu.facom.mehar.sonar.core.model.topology.type.ElementType;
import br.ufu.facom.mehar.sonar.core.util.ObjectUtils;
import br.ufu.facom.mehar.sonar.organizing.configuration.configuration.SCEConfiguration;

@Component
public class TopologyManager {
	private Logger logger = LoggerFactory.getLogger(TopologyManager.class);
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private DeviceService deviceService; 
	
	@Autowired
	private SCEConfiguration configuration;
	
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
	public void listenToElementEvents() {
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				logger.info("Listening to '"+SonarTopics.TOPIC_TOPOLOGY_ELEMENT+"'...");
				eventService.subscribe(SonarTopics.TOPIC_TOPOLOGY_ELEMENT, new NetworkEventAction() {
					@Override
					public void handle(String event, String json) {
						System.out.println("Event: "+event+" JSON:"+json);
						if(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_ADDED.equals(event)) {
							Element element = ObjectUtils.toObject(json, Element.class);
							if(ElementType.DEVICE.equals(element.getTypeElement())){
								try {
									deviceService.configureControllerIfSupported(element, configuration.getSDNSouthSeeds());
									logger.info("Controller configured!");
								} catch(Exception e) {
									logger.error("Error while configuring controllers on element: "+ObjectUtils.toString(element));
								}
							}
						}
					}
				});
			}
		});
	}
}
