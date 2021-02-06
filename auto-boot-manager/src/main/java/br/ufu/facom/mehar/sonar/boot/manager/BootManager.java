package br.ufu.facom.mehar.sonar.boot.manager;

import java.net.InterfaceAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import br.ufu.facom.mehar.sonar.boot.server.exception.DatabasePreparationException;
import br.ufu.facom.mehar.sonar.boot.server.exception.InvalidEndpointException;
import br.ufu.facom.mehar.sonar.client.ndb.configuration.NDBConfiguration;
import br.ufu.facom.mehar.sonar.client.ndb.repository.DatabaseBuilder;
import br.ufu.facom.mehar.sonar.client.ndb.repository.impl.casandra.CassandraGenericRepository;
import br.ufu.facom.mehar.sonar.client.ndb.service.CoreDataService;
import br.ufu.facom.mehar.sonar.client.nem.action.NetworkEventAction;
import br.ufu.facom.mehar.sonar.client.nem.configuration.SonarTopics;
import br.ufu.facom.mehar.sonar.client.nem.service.EventService;
import br.ufu.facom.mehar.sonar.client.nim.component.Component;
import br.ufu.facom.mehar.sonar.client.nim.component.service.ComponentService;
import br.ufu.facom.mehar.sonar.core.model.container.Container;
import br.ufu.facom.mehar.sonar.core.model.container.ContainerStatus;
import br.ufu.facom.mehar.sonar.core.model.core.Controller;
import br.ufu.facom.mehar.sonar.core.model.sonar.Entity;
import br.ufu.facom.mehar.sonar.core.util.IPUtils;
import br.ufu.facom.mehar.sonar.core.util.ObjectUtils;

@org.springframework.stereotype.Component
public class BootManager {

	private Logger logger = LoggerFactory.getLogger(BootManager.class);

	@Autowired
	private ComponentService componentService;

	@Autowired
	private DatabaseBuilder databaseBuilder;

	@Autowired
	private CoreDataService coreService;

	@Autowired
	private EventService eventService;

	@Value("${cim.manager.ip:localhost}")
	private String CIM_IP;

	@Value("${cim.manager.port:8080}")
	private String CIM_PORT;

	@Value("${boot.manager.dhcp.enabled:true}")
	private Boolean DHCP_ENABLED;

	@Value("${boot.manager.ci.enabled:true}")
	private Boolean CI_ENABLED;

	@Value("${boot.manager.ndb.autocreate:true}")
	private Boolean NDB_AUTO_CREATE;

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

		// RUN NDB, NEM and SDN Controller
		Container ndb = checkAndRunComponent(Component.DistributedNetworkDatabase);
		if (ndb == null) {
			logger.error("Unable to boot network without 'DistributedNetworkDatabase'.");
			return;
		}

		Container nem = checkAndRunComponent(Component.NetworkEventManager);
		if (nem == null) {
			logger.error("Unable to boot network without 'NetworkEventManager'.");
			return;
		}

		Container sdn = checkAndRunComponent(Component.SDNController);
		if (sdn == null) {
			logger.error("Unable to boot network without 'SDNController'.");
			return;
		}

		// Wait for Database
		try {
			// wait for database and create it (id it doesn't exist)
			waitAndCreateDatabase("localhost:" + ndb.getAccessPort().get("main"), ndb.getImage());

			// Create Initial Properties
			Properties propertiesBridge = generateProperties(bindInterfaceAddress, ndb, nem, sdn, Boolean.FALSE);
			Properties propertiesHost = generateProperties(bindInterfaceAddress, ndb, nem, sdn, Boolean.TRUE);

			// Verify and Run DHCP
			if (DHCP_ENABLED) {
				checkAndRunSingletonComponent(Component.DHCPServer, propertiesHost);
			}

			Container ci = null;
			if (CI_ENABLED) {
				ci = checkAndRunSingletonComponent(Component.ControllerInterceptor, propertiesBridge);
				if (ci == null) {
					logger.error("Unable to boot network without 'ControllerInterceptor'.");
					return;
				}
			}

			verifyAndCreateController(sdn, ci, bindInterfaceAddress);

//			checkAndRunSingletonComponent(Component.TopologySelfCollectorEntity, propertiesBridge);
//			checkAndRunSingletonComponent(Component.SelfConfigurationEntity, propertiesBridge);

			waitSpecificEntitiesInitialization();
			checkAndRunSingletonComponent(Component.SelfHealingEntity, propertiesBridge);

		} finally {
			finish();
		}
	}

	private void waitSpecificEntitiesInitialization() {
		logger.info("Listening " + SonarTopics.TOPIC_ENTITY_STARTED);
		eventService.subscribe(SonarTopics.TOPIC_ENTITY_STARTED, new NetworkEventAction() {
			@Override
			public void handle(String event, String json) {
				logger.info("-- receiving entity started: " + json);
				Entity entity = ObjectUtils.toObject(json, Entity.class);
				logger.info("-- entity started: " + entity);
				if ("SHE".equalsIgnoreCase(entity.getName())) {
					logger.info("-- SHE started. Sending the catalog to it");
					containerMap.entrySet().forEach(entry -> {
						logger.info("-- Container: " + entry.getKey() + " " + entry.getValue());
						eventService.publish(SonarTopics.TOPIC_CATALOG_CONTAINERS, entry.getValue().get(0));
					});

				}
			}
		});

	}

	private void verifyAndCreateController(Container sdn, Container ci, InterfaceAddress bindInterfaceAddress) {
		boolean controllerAlreadyCreated = false;
		for (Controller controller : coreService.getControllers()) {
			if (controller.getSouth().equals(findEndPoint(Component.SDNController, sdn, "south",
					bindInterfaceAddress.getAddress().getHostAddress()))) {
				controllerAlreadyCreated = true;
			}
		}

		if (!controllerAlreadyCreated) {
			Controller controller = new Controller();
			controller.setIdController(UUID.randomUUID());
			controller.setSouth(findEndPoint(Component.SDNController, sdn, "south",
					bindInterfaceAddress.getAddress().getHostAddress()));
			controller.setNorth(findEndPoint(Component.SDNController, sdn, "north",
					bindInterfaceAddress.getAddress().getHostAddress()));
			if (ci != null) {
				controller.setInterceptor(findEndPoint(Component.ControllerInterceptor, ci, "main",
						bindInterfaceAddress.getAddress().getHostAddress()));
			}
			controller.setStrategy(sdn.getImage());
			if ("onos".equals(sdn.getImage())) {
				controller.setAuthUsername("onos");
				controller.setAuthPassword("rocks");
			} else {
				throw new NotImplementedException(
						"Support to SDN Controller images different of 'onos' is not supported yet! Image:"
								+ sdn.getImage());
			}

			coreService.save(controller);
		}
	}

	private Properties generateProperties(InterfaceAddress bindInterfaceAddress, Container ndb, Container nem,
			Container sdn, Boolean networkHost) {
		Properties propertiesBridge = new Properties();

		propertiesBridge.setProperty("SONAR_SERVER_LOCAL_IP_ADDRESS",
				bindInterfaceAddress.getAddress().getHostAddress());
		propertiesBridge.setProperty("SONAR_SERVER_LOCAL_IP_MASK",
				IPUtils.prefixToMask(bindInterfaceAddress.getNetworkPrefixLength()));
		propertiesBridge.setProperty("SONAR_SERVER_LOCAL_IP_BROADCAST",
				bindInterfaceAddress.getBroadcast().getHostAddress());

		propertiesBridge.setProperty("SONAR_SERVER_SEEDS", bindInterfaceAddress.getAddress().getHostAddress());
		if (networkHost) {
			propertiesBridge.setProperty("NDB_SEEDS",
					findEndPoint(Component.DistributedNetworkDatabase, ndb, "main", "localhost"));
			propertiesBridge.setProperty("NEM_SEEDS",
					findEndPoint(Component.NetworkEventManager, nem, "main", "localhost"));
		} else {
			propertiesBridge.setProperty("NDB_SEEDS", findEndPoint(Component.DistributedNetworkDatabase, ndb, "main",
					bindInterfaceAddress.getAddress().getHostAddress()));
			propertiesBridge.setProperty("NEM_SEEDS", findEndPoint(Component.NetworkEventManager, nem, "main",
					bindInterfaceAddress.getAddress().getHostAddress()));
		}

		propertiesBridge.setProperty("NDB_STRATEGY", ndb.getImage());
		propertiesBridge.setProperty("NEM_STRATEGY", nem.getImage());

		return propertiesBridge;
	}

	private void finish() {
		CassandraGenericRepository.clusterFinish();
	}

	private void waitAndCreateDatabase(String endpoint, String strategy) {
		NDBConfiguration.setSeeds(endpoint);
		NDBConfiguration.setStrategy(strategy);

		logger.info("Waiting " + Component.DistributedNetworkDatabase + "...");
		boolean ndbUp = false;
		int attempt = 1;
		while (!ndbUp) {
			try {
				// Sleep
				Thread.sleep(2000);

				if (!databaseBuilder.isBuilt()) {
					if (NDB_AUTO_CREATE) {
						databaseBuilder.buildOrAlter();
					}
				}

				ndbUp = true;
			} catch (InterruptedException e) {
				throw new DatabasePreparationException("Error wating for NDB node to run.", e);
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
				if (container.getServer().equals("local") || container.getServer().equals("localhost")
						|| container.getServer().equals("127.0.0.1")) {
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
				} else {
					Container container = containerList.get(0);

					if (ContainerStatus.RUNNING_STATE.equals(container.getStatus())) {
						logger.info("| '" + component + "' is already created and running with id " + container.getId()
								+ "!");
						logger.debug(ObjectUtils.toString(container));

						return container;

					} else {
						logger.info("| '" + component + "' is already created but not running with id "
								+ container.getId() + ". Running container...");
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
