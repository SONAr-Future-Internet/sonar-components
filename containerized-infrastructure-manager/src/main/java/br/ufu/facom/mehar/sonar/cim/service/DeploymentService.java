package br.ufu.facom.mehar.sonar.cim.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ufu.facom.mehar.sonar.cim.model.Container;
import br.ufu.facom.mehar.sonar.cim.model.Registry;

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
			logger.error("Error starting components on 'deployment-plan'.");
		}
	}

	public void run(Registry registry) {
		for (Registry.Server server : registry.getServers()) {
			List<Container> containers = containerService.getRunningContainers(server.getId());
			Registry activeRegistry = generateRegistry(containers);
			Registry.Server activeServer = activeRegistry.getServer(server.getId());

			for (Registry.Namespace namespace : server.getNamespaces()) {
				if (activeServer.getNamespace(namespace.getId()) == null) {
					activate(server, namespace);
				} else {
					Registry.Namespace activeNamespace = activeServer.getNamespace(namespace.getId());
					for (Registry.Image image : namespace.getImages()) {

						if (activeNamespace.getImage(image.getId()) == null) {
							activate(server, namespace, image);
						} else {
							Registry.Image activeImage = activeNamespace.getImage(image.getId());
							
							if(image.getContainers().isEmpty()) {
								if(activeImage.getContainers().isEmpty()) {
									activate(server, namespace, image);
								}
							}else {							
								for (Registry.Container service : image.getContainers()) {
									if (activeImage.getContainerById(service.getId()) == null) {
										if (activeImage.getContainerByOutPort(service.getOutPort()) == null) {
											activate(server, namespace, image, service);
										}
									}
								}
							}
						}
					}
				}
			}

			for (Container container : containers) {
				Registry.Namespace namespace = server.getNamespace(container.getNamespace());
				if (namespace == null) {
					registryService.register(container);
				} else {

				}
			}
		}
	}

	private void activate(Registry.Server server, Registry.Namespace namespace) {
		for (Registry.Image image : namespace.getImages()) {
			this.activate(server, namespace, image);
		}
	}

	private void activate(Registry.Server server, Registry.Namespace namespace, Registry.Image image) {
		if(image.getContainers().isEmpty()) {
			containerService.run(server.getId(), namespace.getId(), image.getId());
		}else {
			for (Registry.Container service : image.getContainers()) {
				this.activate(server, namespace, image, service);
			}
		}
	}

	private void activate(Registry.Server server, Registry.Namespace namespace, Registry.Image image, Registry.Container service) {
		containerService.run(server.getId(), namespace.getId(), image.getId(), service.getId(), service.getInPort(), service.getOutPort());
	}

	private Registry generateRegistry(List<Container> containers) {
		Registry registry = new Registry();
		for (Container container : containers) {
			Registry.Server server = registry.getServer(container.getServer());
			if (server == null) {
				server = registry.addServer(container.getServer());
			}

			Registry.Namespace namespace = server.getNamespace(container.getNamespace());
			if (namespace == null) {
				namespace = server.addNamespace(container.getNamespace());
			}

			Registry.Image image = namespace.getImage(container.getImageName());
			if (image == null) {
				image = namespace.addImage(container.getImageName());
			}

			Registry.Container service = null;
			if (container.getId() != null && !container.getId().isEmpty()) {
				service = image.getContainerById(container.getId());
			} else {
				service = image.getContainerByOutPort(container.getOutPort());
			}

			if (service == null) {
				image.addContainer(container.getId(), container.getStatus(), container.getInPort(), container.getOutPort());
			}
		}

		return registry;
	}

}
