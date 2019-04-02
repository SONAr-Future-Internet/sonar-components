package br.ufu.facom.mehar.sonar.cim.service;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ufu.facom.mehar.sonar.cim.exception.ContainerAlreadyRunningException;
import br.ufu.facom.mehar.sonar.core.model.container.Container;
import br.ufu.facom.mehar.sonar.core.model.container.Registry;

@Service
public class DeploymentService {
	private Logger logger = Logger.getLogger(DeploymentService.class);

	@Autowired
	private ContainerService containerService;

	@Autowired
	private RegistryService registryService;

	@EventListener(ApplicationReadyEvent.class)
	public void init() {

		try {
			ClassLoader classLoader = getClass().getClassLoader();
			File file = new File(classLoader.getResource("deployment-plan.cimd").getFile());

			ObjectMapper objectMapper = new ObjectMapper();
			Registry registry = objectMapper.readValue(file, Registry.class);

			this.run(registry);

		} catch (IOException e) {
			logger.error("Error starting components on 'deployment-plan'.", e);
		}
	}

	public void run(Registry registry) {
		for (Container container : registry.getContainers()) {
			try {
				logger.info("Running " + container.getImageNameWithNamespaceAndVersion() + "... ");
				Container containerRunning = containerService.run(container);
				registryService.register(containerRunning);
				logger.info("     - " + container.getImageNameWithNamespaceAndVersion() + " started!");
			} catch (ContainerAlreadyRunningException e) {
				logger.info("     - " + container.getImageNameWithNamespaceAndVersion()
						+ " is already running. Details:" + ReflectionToStringBuilder.toString(container), e);
			}
		}
	}
}
