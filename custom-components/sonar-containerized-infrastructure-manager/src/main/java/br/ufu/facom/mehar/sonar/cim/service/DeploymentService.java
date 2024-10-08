package br.ufu.facom.mehar.sonar.cim.service;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ufu.facom.mehar.sonar.cim.exception.ContainerAlreadyRunningException;
import br.ufu.facom.mehar.sonar.core.model.container.Container;
import br.ufu.facom.mehar.sonar.core.model.container.Registry;

@Service
public class DeploymentService {
	private Logger logger = LoggerFactory.getLogger(DeploymentService.class);

	@Autowired
	private ContainerService containerService;

	@Autowired
	private RegistryService registryService;
	
	@Value("${cim.serverLocalEnabled:true}")
	private Boolean SERVER_LOCAL_ENABLED;
	
	@Value("${cim.runDeploymentPlan:false}")
	private Boolean RUN_DEPLOYMENT_PLAN;

	@EventListener(ApplicationReadyEvent.class)
	public void init() {

		try {
			if(RUN_DEPLOYMENT_PLAN) {
				ClassLoader classLoader = getClass().getClassLoader();
				File file = new File(classLoader.getResource("deployment-plan.json").getFile());
	
				ObjectMapper objectMapper = new ObjectMapper();
				Registry registry = objectMapper.readValue(file, Registry.class);
	
				this.run(registry);
			}
			
			if(SERVER_LOCAL_ENABLED) {
				registryService.registerServer("local");
				for(Container container : containerService.getRunningContainersByServer("local")) {
					registryService.register(container);
				}
			}

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
				logger.info("     - " + container.getImageNameWithNamespaceAndVersion() + " is already running. Details:" + ReflectionToStringBuilder.toString(container), e);
			}
		}
	}
}
