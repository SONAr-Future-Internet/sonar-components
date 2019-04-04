package br.ufu.facom.mehar.sonar.boot.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import br.ufu.facom.mehar.sonar.boot.server.exception.InvalidEndpointException;
import br.ufu.facom.mehar.sonar.client.cim.Component;
import br.ufu.facom.mehar.sonar.client.cim.service.ComponentService;
import br.ufu.facom.mehar.sonar.core.model.container.Container;
import br.ufu.facom.mehar.sonar.core.model.container.ContainerStatus;
import br.ufu.facom.mehar.sonar.core.util.ObjectUtils;

@org.springframework.stereotype.Component
public class BootManager {

	private Logger logger = Logger.getLogger(BootManager.class);

	@Autowired
	private ComponentService componentService;

	@Value("${cim.manager.ip:localhost}")
	private String CIM_IP;

	@Value("${cim.manager.port:8080}")
	private String CIM_PORT;

	@Value("${boot.manager.dhcp.enabled:true}")
	private Boolean DHCP_ENABLED;

	private Map<Component, List<Container>> containerMap;

	@EventListener(ApplicationReadyEvent.class)
	public void run() {
		logger.info("Booting network...");
		// Query already created components
		containerMap = componentService.get(CIM_IP);

		Container nddb = checkAndRunComponent(Component.DistributedNetworkDatabase);
		if (nddb == null) {
			logger.fatal("Unable to boot network without 'DistributedNetworkDatabase'.");
			return;
		}

		Container nem = checkAndRunComponent(Component.NetworkEventManager);
		if (nem == null) {
			logger.fatal("Unable to boot network without 'NetworkEventManager'.");
			return;
		}

		Properties properties = new Properties();
		String nddbEndpoint = findEndPoint(Component.DistributedNetworkDatabase, nddb, "main");
		String nemEndpoint = findEndPoint(Component.NetworkEventManager, nem, "main");
		properties.setProperty("NDDB_SEEDS", nddbEndpoint);
		properties.setProperty("NEM_SEEDS", nemEndpoint);

		// Verify and Run DHCP
		if (DHCP_ENABLED) {
			checkAndRunSingletonComponent(Component.DHCPServer, properties);
		}

		// checkAndRunComponent(Component.TopologySelfCollectorEntity, properties);
	}

	private String findEndPoint(Component component, Container container, String accessPort) {
		String port = container.getAccessPort().get(accessPort);
		if (port != null) {
			if (container.getServer() != null && !container.getServer().isEmpty()
					&& container.getServer().equals("local")) {
				return container.getServer() + ":" + port;
			} else {
				throw new InvalidEndpointException("Invalid endpoint " + accessPort + " of '" + component
						+ "'. Server is invalid! server = " + container.getServer() + ".");
			}
		} else {
			throw new InvalidEndpointException("Invalid endpoint " + accessPort + " of '" + component
					+ "'. Port not specified! ports: " + container.getAccessPort() + ".");
		}
	}

	private Container checkAndRunSingletonComponent(Component component, Properties properties) {
		try {
			logger.info("Starting '" + component + "'...");

			List<Container> containerList = containerMap.get(component);
			
			if (containerList != null && !containerList.isEmpty()) {
				for (Container container : containerList) {
					logger.info("| '" + component + "' is already created! Removing container with id " + container.getId()
							+ "...");
					container = componentService.deleteComponent(CIM_IP, component);
					if (ContainerStatus.REMOVED_STATE.equals(container.getStatus())) {
						logger.info("| '" + component + "' removed!");
					} else {
						logger.info("| '" + component + "' not removed! Status: " + container.getStatus()
								+ ". Ignoring Error.");
						return null;
					}
				}
			}

			Container container = componentService.runComponent(CIM_IP, component, properties);
			logger.info("| '" + component + "' is running.");
			logger.debug(ObjectUtils.toString(container));
			containerMap.put(component, new ArrayList<Container>(Arrays.asList(container)));
			return container;
		} catch (Exception e) {
			logger.error("| Error running '" + component + "' component. Ignoring error.", e);
			return null;
		}
	}

	private Container checkAndRunComponent(Component component) {
		try {
			logger.info("Starting '" + component + "'...");
			List<Container> containerList = containerMap.get(component);

			if (containerList == null) {
				containerList = new ArrayList<Container>();
				containerMap.put(component, containerList);
			}

			for (Container container : containerList) {
				if (ContainerStatus.RUNNING_STATE.equals(container.getStatus())) {
					logger.info(
							"| '" + component + "' is already created and running with id " + container.getId() + "!");
					logger.debug(ObjectUtils.toString(container));

					return container;

				} else {
					logger.info("| '" + component + "' is already created but not running with id " + container.getId()
							+ ". Running container...");
					Container newContainer = componentService.runComponent(CIM_IP, component);
					logger.info("| '" + component + "' is running!");
					logger.debug(ObjectUtils.toString(container));

					containerList.remove(container);
					containerList.add(newContainer);

					return newContainer;
				}
			}

			Container container = componentService.runComponent(CIM_IP, component);
			logger.info("| '" + component + "' is running.");
			logger.debug(ObjectUtils.toString(container));

			containerList.add(container);

			return container;
		} catch (Exception e) {
			logger.error("| Error running '" + component + "' component. Ignoring error.", e);
			return null;
		}
	}
}
