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
import br.ufu.facom.mehar.sonar.client.ndb.configuration.NDBConfiguration;
import br.ufu.facom.mehar.sonar.client.ndb.repository.DatabaseBuilder;
import br.ufu.facom.mehar.sonar.client.ndb.repository.impl.casandra.CassandraGenericRepository;
import br.ufu.facom.mehar.sonar.client.nim.component.Component;
import br.ufu.facom.mehar.sonar.client.nim.component.service.ComponentService;
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

		//RUN NDB, NEM and SDN Controller
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
		
//		Container sdn = checkAndRunComponent(Component.SDNController);
//		if (sdn == null) {
//			logger.error("Unable to boot network without 'SDNController'.");
//			return;
//		}

		//Wait for Database
		try {
		prepareDatabase("localhost:" + ndb.getAccessPort().get("main"), ndb.getImage());

		//Create Database (id it doesn't exist)
		if (NDB_AUTO_CREATE) {
			if (!databaseBuilder.isBuilt()) {
				databaseBuilder.buildOrAlter();
			}
		}

		// Verify and Run DHCP
		if (DHCP_ENABLED) {
			Properties propertiesDHCP = new Properties();
			propertiesDHCP.setProperty("NDB_SEEDS", findEndPoint(Component.DistributedNetworkDatabase, ndb, "main", "localhost"));
			propertiesDHCP.setProperty("NDB_STRATEGY", ndb.getImage());
			propertiesDHCP.setProperty("NEM_SEEDS", findEndPoint(Component.NetworkEventManager, nem, "main", "localhost"));
			propertiesDHCP.setProperty("NEM_STRATEGY", nem.getImage());
			checkAndRunSingletonComponent(Component.DHCPServer, propertiesDHCP);
		}

		//Create Initial Properties
		Properties properties = new Properties();
		properties.setProperty("NDB_SEEDS", findEndPoint(Component.DistributedNetworkDatabase, ndb, "main", bindInterfaceAddress.getAddress().toString()));
		properties.setProperty("NEM_SEEDS", findEndPoint(Component.NetworkEventManager, nem, "main", bindInterfaceAddress.getAddress().toString()));
//		properties.setProperty("SDN_SOUTH_SEEDS", findEndPoint(Component.SDNController, sdn, "south", bindInterfaceAddress.getAddress().toString()));
//		properties.setProperty("SDN_NORTH_SEEDS", findEndPoint(Component.SDNController, sdn, "north", bindInterfaceAddress.getAddress().toString()));
		properties.setProperty("NDB_STRATEGY", ndb.getImage());
		properties.setProperty("NEM_STRATEGY", nem.getImage());
//		properties.setProperty("SDN_STRATEGY", sdn.getImage());

		// checkAndRunComponent(Component.TopologySelfCollectorEntity, properties);
		} finally {
			finish();
		}
	}

	private void finish() {
		CassandraGenericRepository.clusterFinish();
	}

	private void prepareDatabase(String endpoint, String strategy) {
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
				logger.info(" | not yet. Attempt: " + (attempt++), e);
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
