package br.ufu.facom.mehar.sonar.organizing.healing.manager;

import java.beans.Encoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import br.ufu.facom.mehar.sonar.client.nem.action.NetworkEventAction;
import br.ufu.facom.mehar.sonar.client.nem.configuration.SonarTopics;
import br.ufu.facom.mehar.sonar.client.nem.service.EventService;
import br.ufu.facom.mehar.sonar.core.model.container.Container;
import br.ufu.facom.mehar.sonar.core.model.sonar.Entity;
import br.ufu.facom.mehar.sonar.core.model.sonar.EntityGroup;
import br.ufu.facom.mehar.sonar.core.util.ObjectUtils;

@Component
public class CatalogManager {
	private Logger logger = LoggerFactory.getLogger(CatalogManager.class);

	@Autowired
	private EventService eventService;

	@EventListener(ApplicationReadyEvent.class)
	public void listenToCatalogEvents() {
		logger.info("Listening " + SonarTopics.TOPIC_CATALOG_CONTAINERS);
		eventService.subscribe(SonarTopics.TOPIC_CATALOG_CONTAINERS, new NetworkEventAction() {
			@Override
			public void handle(String event, String json) {
				logger.info("-- receiving catalog information: " + json);
				Container container = ObjectUtils.toObject(json, Container.class);
				logger.info("-- container information received: " + container);
			}
		});

		eventService.publish(SonarTopics.TOPIC_ENTITY_STARTED, new Entity("SHE", EntityGroup.getByKey("SOE")));
	}

}
