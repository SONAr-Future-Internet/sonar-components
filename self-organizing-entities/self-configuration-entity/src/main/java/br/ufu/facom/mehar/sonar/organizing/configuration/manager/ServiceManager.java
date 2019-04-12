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

@Component
public class ServiceManager {
	private Logger logger = LoggerFactory.getLogger(ServiceManager.class);
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	@Qualifier("taskScheduler")
    private TaskExecutor taskExecutor;
	
	@EventListener(ApplicationReadyEvent.class)
	public void listenToServiceManagementEvents() {
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				logger.info("Listening to '"+SonarTopics.TOPIC_SERVICE+"'...");
				
				eventService.subscribe(SonarTopics.TOPIC_SERVICE, new NetworkEventAction() {
					@Override
					public void handle(String event, String json) {
						System.out.println("Event: "+event+" JSON:"+json);
					}
				});
			}
		});
	}
}
