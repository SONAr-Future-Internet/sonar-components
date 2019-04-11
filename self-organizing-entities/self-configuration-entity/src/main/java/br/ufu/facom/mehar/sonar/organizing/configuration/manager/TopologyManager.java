package br.ufu.facom.mehar.sonar.organizing.configuration.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import br.ufu.facom.mehar.sonar.client.nem.action.NetworkEventAction;
import br.ufu.facom.mehar.sonar.client.nem.configuration.SonarTopics;
import br.ufu.facom.mehar.sonar.client.nem.service.EventService;

@Component
public class TopologyManager {
	@Autowired
	EventService eventService;
	
	@Autowired
	@Qualifier("taskScheduler")
    private TaskExecutor taskExecutor;
	
	@EventListener(ApplicationReadyEvent.class)
	public void listenToTopologyLinkUpdateEvents() {
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
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
				eventService.subscribe(SonarTopics.TOPIC_TOPOLOGY_ELEMENT, new NetworkEventAction() {
					@Override
					public void handle(String event, String json) {
						System.out.println("Event: "+event+" JSON:"+json);
					}
				});
			}
		});
	}
}
