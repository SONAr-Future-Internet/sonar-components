package br.ufu.facom.mehar.sonar.boot.manager;

import java.net.InterfaceAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import br.ufu.facom.mehar.sonar.boot.server.exception.DatabasePreparationException;
import br.ufu.facom.mehar.sonar.boot.server.exception.InvalidEndpointException;
import br.ufu.facom.mehar.sonar.client.cim.Component;
import br.ufu.facom.mehar.sonar.client.cim.service.ComponentService;
import br.ufu.facom.mehar.sonar.client.dndb.configuration.DNDBConfiguration;
import br.ufu.facom.mehar.sonar.client.dndb.repository.DatabaseBuilder;
import br.ufu.facom.mehar.sonar.core.model.container.Container;
import br.ufu.facom.mehar.sonar.core.model.container.ContainerStatus;
import br.ufu.facom.mehar.sonar.core.util.IPUtils;
import br.ufu.facom.mehar.sonar.core.util.ObjectUtils;

@org.springframework.stereotype.Component
public class BootManager {

	private Logger logger = LoggerFactory.getLogger(BootManager.class);

	@Autowired
	private ComponentService componentService;

	@Autowired
	private DatabaseBuilder databaseBuilder;

	@Value("${cim.manager.ip:localhost}")
	private String CIM_IP;

	@Value("${cim.manager.port:8080}")
	private String CIM_PORT;

	@Value("${boot.manager.dhcp.enabled:true}")
	private Boolean DHCP_ENABLED;

	@Value("${boot.manager.dndb.autocreate:true}")
	private Boolean DNDB_AUTO_CREATE;

	private Map<Component, List<Container>> containerMap;

	@EventListener(ApplicationReadyEvent.class)
	public void run() {
		logger.info("Booting network...");

		InterfaceAddress bindInterfaceAddress = IPUtils.searchActiveInterfaceAddress();
		if (bindInterfaceAddress == null || bindInterfaceAddress.getAddress() == null) {
			logger.error("Cannot find a address to bind!");
			return;
		}

		logger.info("Using address: " + bindInterfaceAddress.getAddress().toString());

		// Query already created components
		containerMap = componentService.get(CIM_IP);

		Container dndb = checkAndRunComponent(Component.DistributedNetworkDatabase);
		if (dndb == null) {
			logger.error("Unable to boot network without 'DistributedNetworkDatabase'.");
			return;
		}

		Container nem = checkAndRunComponent(Component.NetworkEventManager);
		if (nem == null) {
			logger.error("Unable to boot network without 'NetworkEventManager'.");
			return;
		}

		prepareDatabase("localhost:" + dndb.getAccessPort().get("main"), dndb.getImage());

		if (DNDB_AUTO_CREATE) {
			if (!databaseBuilder.isBuilt()) {
				databaseBuilder.buildOrAlter();
			}
		}

		// Verify and Run DHCP
		if (DHCP_ENABLED) {
			Properties propertiesDHCP = new Properties();
			propertiesDHCP.setProperty("DNDB_SEEDS",
					findEndPoint(Component.DistributedNetworkDatabase, dndb, "main", "localhost"));
			propertiesDHCP.setProperty("DNDB_STRATEGY", dndb.getImage());
			propertiesDHCP.setProperty("NEM_SEEDS",
					findEndPoint(Component.NetworkEventManager, nem, "main", "localhost"));
			propertiesDHCP.setProperty("NEM_STRATEGY", nem.getImage());
			checkAndRunSingletonComponent(Component.DHCPServer, propertiesDHCP);
		}

		Properties properties = new Properties();
		String dndbEndpoint = findEndPoint(Component.DistributedNetworkDatabase, dndb, "main",
				bindInterfaceAddress.getAddress().toString());
		String nemEndpoint = findEndPoint(Component.NetworkEventManager, nem, "main",
				bindInterfaceAddress.getAddress().toString());
		properties.setProperty("DNDB_SEEDS", dndbEndpoint);
		properties.setProperty("DNDB_STRATEGY", dndb.getImage());
		properties.setProperty("NEM_SEEDS", nemEndpoint);
		properties.setProperty("NEM_STRATEGY", nem.getImage());

		// checkAndRunComponent(Component.TopologySelfCollectorEntity, properties);
	}

	private void prepareDatabase(String endpoint, String strategy) {
		DNDBConfiguration.setSeeds(endpoint);
		DNDBConfiguration.setStrategy(strategy);

		logger.info("Waiting " + Component.DistributedNetworkDatabase + "...");
		boolean dndbUp = false;
		int attempt = 1;
		while (!dndbUp) {
			try {
				// Sleep
				Thread.sleep(2000);

				if (!databaseBuilder.isBuilt()) {
					if (DNDB_AUTO_CREATE) {
						databaseBuilder.buildOrAlter();
					}
				}

				dndbUp = true;
			} catch (InterruptedException e) {
				throw new DatabasePreparationException("Error wating for DNDB node to run.", e);
			} catch (Exception e) {
				logger.info(" | not yet. Attempt: " + (attempt++));
			}
		}
	}

	private String findEndPoint(Component component, Container container, String accessPort,
			String localServerAddress) {
		String port = container.getAccessPort().get(accessPort);
		if (port != null) {
			if (container.getServer() != null && !container.getServer().isEmpty()) {
				if (container.getServer().equals("local") || container.getServer().equals("localhost")) {
					return localServerAddress + ":" + port;
				} else {
					return container.getServer() + ":" + port;
				}
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
				if (containerList.size() > 1) {
					for (Container container : containerList) {
						logger.info("| '" + component + "' is already created! Removing container with id "
								+ container.getId() + "...");
						container = componentService.deleteComponent(CIM_IP, container.getId(), component);
						if (ContainerStatus.REMOVED_STATE.equals(container.getStatus())) {
							logger.info("| '" + component + "' removed!");
						} else {
							logger.info("| '" + component + "' not removed! Status: " + container.getStatus()
									+ ". Ignoring Error.");
							return null;
						}
					}
				}else {
					Container container = containerList.get(0);
					
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
