package br.ufu.facom.mehar.sonar.collectors.topology.service;

import java.net.InterfaceAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import br.ufu.facom.mehar.sonar.client.dndb.service.TopologyService;
import br.ufu.facom.mehar.sonar.client.nem.action.NetworkEventAction;
import br.ufu.facom.mehar.sonar.client.nem.configuration.SonarTopics;
import br.ufu.facom.mehar.sonar.client.nem.service.EventService;
import br.ufu.facom.mehar.sonar.collectors.topology.manager.lldp.LldpDiscoverManager;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;
import br.ufu.facom.mehar.sonar.core.util.CountingLatch;
import br.ufu.facom.mehar.sonar.core.util.IPUtils;
import br.ufu.facom.mehar.sonar.core.util.ObjectUtils;
import br.ufu.facom.mehar.sonar.core.util.Pair;

@Component
public class DiscoveryService {

	private Logger logger = LoggerFactory.getLogger(DiscoveryService.class);

	@Autowired
	private EventService eventService;

	@Autowired
	private TopologyService topologyService;

	@Autowired
	private LldpDiscoverManager lldpDiscoverManager;

	@Value("${topology.scoe.discovery.element.updateInterval:600000}")
	private Integer elementUpdateInterval;

	@Value("${topology.scoe.discovery.strategy.flooding.first:192.168.0.1}")
	private String floodingIntervalFirst;

	@Value("${topology.scoe.discovery.strategy.flooding.last:192.168.0.254}")
	private String floodingIntervalLast;

	private String instance = "tscoe";
	{
		InterfaceAddress interfaceAddress = IPUtils.searchActiveInterfaceAddress();
		if (interfaceAddress != null && interfaceAddress.getAddress() != null) {
			instance += "-" + interfaceAddress.getAddress().toString();
		}
	}

	/**
	 * Schedule and Listerner
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void listenToEvents() throws InterruptedException {
		eventService.subscribe(SonarTopics.TOPIC_TOPOLOGY_PORT_IP_ASSIGNED, new NetworkEventAction() {
			@Override
			public void handle(String event, String json) {
				Port portEvent = ObjectUtils.toObject(json, Port.class);
				String ip = portEvent.getIpAddress();
				String mac = IPUtils.normalizeMAC(portEvent.getMacAddress());

				Pair<Element, Port> pairByMac = topologyService.getElementAndPortByPortMacAddress(mac);
				Pair<Element, Port> pairByIp = topologyService.getElementAndPortByPortIPAddress(ip);

				if ((pairByMac == null || pairByMac.getFirst() == null)
						&& (pairByIp == null || pairByIp.getFirst() == null)) {
					// New assignment
					Element element = new Element();
					element.setManagementIPAddressList(Sets.newHashSet(ip));
					element.setPortList(Sets.newHashSet(portEvent));
					discoverByIPAssignment(element);
				} else {
					if (pairByIp != null && pairByMac != null && pairByIp.getFirst() != null
							&& pairByMac.getFirst() != null) {
						if (pairByMac.getFirst().equals(pairByIp.getFirst())) {
							// Reassignment
							// Do nothing! Just wait for the normal discovery schedule.
						} else {
							// Assignment conflict

							// Remove old IP assignment
							if (pairByMac.getSecond().getIpAddress() != null) {
								if (pairByMac.getFirst().getManagementIPAddressList() != null) {
									pairByMac.getFirst().getManagementIPAddressList()
											.remove(pairByMac.getSecond().getIpAddress());
								}
							}

							// Remove conflict IP assignment
							pairByMac.getFirst().getManagementIPAddressList().remove(ip);
							pairByMac.getSecond().setIpAddress(null);

							// Set new assignment
							if (pairByMac.getFirst().getManagementIPAddressList() == null) {
								pairByMac.getFirst().setManagementIPAddressList(new HashSet<String>());
							}
							pairByMac.getFirst().getManagementIPAddressList().add(ip);
							pairByMac.getSecond().setIpAddress(ip);

							// Set Discovery Fields
							setDiscoveryFields(pairByIp.getFirst(), new Date(), instance, "IP-ASSIGNMENT", "DHCP");
							setDiscoveryFields(pairByMac.getFirst(), new Date(), instance, "IP-ASSIGNMENT", "DHCP");

							// Update (but not discovery)
							update(pairByIp.getFirst());
							update(pairByMac.getFirst());
							update(pairByIp.getSecond());
							update(pairByMac.getSecond());

						}
					} else {
						if (pairByMac != null && pairByIp.getFirst() != null
								&& (pairByIp == null || pairByIp.getFirst() != null)) {
							// Assignment to an already created element
							// Set new assignment
							if (pairByMac.getFirst().getManagementIPAddressList() == null) {
								pairByMac.getFirst().setManagementIPAddressList(new HashSet<String>());
							}
							pairByMac.getFirst().getManagementIPAddressList().add(ip);
							pairByMac.getSecond().setIpAddress(ip);

							// Set Discovery Fields
							setDiscoveryFields(pairByMac.getFirst(), new Date(), instance, "IP-ASSIGNMENT", "DHCP");

							// Update (but not discovery)
							update(pairByMac.getFirst());
							update(pairByMac.getSecond());
						} else {
							// Assignment to an already created element not discovery yet
							// Do nothing! Just wait for the normal discovery schedule.
						}
					}
				}
			}
		});
	}

	@Scheduled(fixedDelayString = "${topology.scoe.discovery.strategy.bfs.interval:600000}")
	public void discoveryDFS() throws InterruptedException {
		logger.info("Discovering network elements using 'Depth-First Search' with local server as root...");

		final Set<Port> portsToRemove = new HashSet<Port>();
		final Set<String> ipsToRemove = new HashSet<String>();

		InterfaceAddress interfaceAddress = IPUtils.searchActiveInterfaceAddress();
		if (interfaceAddress != null && interfaceAddress.getAddress() != null) {
			String rootIp = interfaceAddress.getAddress().toString();
			Element element = topologyService.getElementByIPAddress(rootIp);
			if (element == null) {
				element = new Element();
				element.setTypeElement(Element.TYPE_SERVER);
				element.setManagementIPAddressList(Sets.newHashSet(rootIp));
			}

			final Map<String, Port> macToPort = new HashMap<String, Port>();
			final Map<UUID, Set<Port>> idElementToPortSet = new HashMap<UUID, Set<Port>>();
			for (Port p : topologyService.getPorts()) {
				macToPort.put(p.getMacAddress(), p);

				if (!idElementToPortSet.containsKey(p.getIdElement())) {
					idElementToPortSet.put(p.getIdElement(), Sets.newHashSet(p));
				} else {
					idElementToPortSet.get(p.getIdElement()).add(p);
				}

			}

			final Map<UUID, Element> idToElement = new HashMap<UUID, Element>();
			for (Element e : topologyService.getElements()) {
				idToElement.put(e.getIdElement(), e);
			}

			// DFS starting from server
			final Stack<String> ipsToDiscovery = new Stack<String>();
			final Set<String> ipsDiscovered = new HashSet<String>();

			// Root of DFS
			ipsToDiscovery.push(element.getManagementIPAddressList().iterator().next());
			ipsDiscovered.add(element.getManagementIPAddressList().iterator().next());

			ExecutorService taskExecutor = Executors.newFixedThreadPool(4);
			final CountingLatch latch = new CountingLatch(0);
			while (latch.getCount() > 0 || !ipsToDiscovery.isEmpty()) {
				while (!ipsToDiscovery.isEmpty()) {
					final String currentIp = ipsToDiscovery.pop();
					latch.countUp();
					taskExecutor.execute(new Runnable() {
						@Override
						public void run() {
							try {
								Element element = lldpDiscoverManager.discover(currentIp);
								if (element != null) {
									Set<String> currentPortMacs = new HashSet<String>();
									Element elementPersisted = null;
									Set<Port> notSavedPorts = new HashSet<Port>();
									for (Port port : element.getPortList()) {
										currentPortMacs.add(port.getMacAddress());
										// Save Or Update
										Port portPersisted = macToPort.get(port.getMacAddress());
										if (portPersisted != null) {
											merge(portPersisted, port);
											if (elementPersisted == null) {
												elementPersisted = idToElement.get(portPersisted.getIdElement());
											}
										} else {
											notSavedPorts.add(port);
										}
									}

									// Save or Update Element
									if (elementPersisted != null) {
										setDiscoveryFields(elementPersisted, new Date(), instance, "ExpiredTime",
												"LLDP");
										merge(elementPersisted, element);
										for (Port port : notSavedPorts) {
											port.setIdElement(elementPersisted.getIdElement());
											save(port);
										}

										for (Port port : idElementToPortSet.get(elementPersisted.getIdElement())) {
											if (!currentPortMacs.contains(port.getMacAddress())) {
												portsToRemove.add(port);
											}
										}
									} else {
										setDiscoveryFields(element, new Date(), instance, "ExpiredTime", "LLDP");
										saveCascade(element);
									}
								} else {
									ipsToRemove.add(currentIp);
								}
							} finally {
								latch.countDown();
							}
						}
					});
				}

				try {
					if (latch.getCount() > 0 || !ipsToDiscovery.isEmpty()) {
						logger.debug("Waiting... " + latch.getCount() + " discovery tasks running and "
								+ ipsToDiscovery.size() + " devices to discovery.");
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			taskExecutor.shutdown();
		}

		// Updating Topology
		Set<Port> portsChanged = new HashSet<Port>();
		portsChanged.addAll(removePorts(portsToRemove));
		portsChanged.addAll(linkElements());
		portsChanged.addAll(removeElements(ipsToRemove));

		if (!portsChanged.isEmpty()) {
			eventService.publish(SonarTopics.TOPIC_TOPOLOGY_LINKS_CHANGED, portsChanged);
		}
		
		logger.info("Discovery using 'Depth-First Search' concluded!");
	}

	@Scheduled(fixedDelayString = "${topology.scoe.discovery.strategy.flooding.interval:60000000}", initialDelayString = "${topology.scoe.discovery.strategy.flooding.interval:60000000}")
	public void discoveryFlooding() throws InterruptedException {
		logger.info("Discovering network elements using 'Flooding' with interval "+floodingIntervalFirst+"-"+floodingIntervalLast+"...");

		final Set<Port> portsToRemove = new HashSet<Port>();
		final Set<String> ipsToRemove = new HashSet<String>();
		final Stack<String> ipsToDiscovery = new Stack<String>();
		String ip = floodingIntervalFirst;

		while (!ip.equals(floodingIntervalLast)) {
			ipsToDiscovery.add(ip);
			ip = IPUtils.nextIP(ip);
		}

		if (!ipsToDiscovery.isEmpty()) {
			final Map<String, Port> macToPort = new HashMap<String, Port>();
			final Map<UUID, Set<Port>> idElementToPortSet = new HashMap<UUID, Set<Port>>();
			for (Port p : topologyService.getPorts()) {
				macToPort.put(p.getMacAddress(), p);

				if (!idElementToPortSet.containsKey(p.getIdElement())) {
					idElementToPortSet.put(p.getIdElement(), Sets.newHashSet(p));
				} else {
					idElementToPortSet.get(p.getIdElement()).add(p);
				}

			}

			final Map<UUID, Element> idToElement = new HashMap<UUID, Element>();
			for (Element e : topologyService.getElements()) {
				idToElement.put(e.getIdElement(), e);
			}

			ExecutorService taskExecutor = Executors.newFixedThreadPool(4);
			final CountingLatch latch = new CountingLatch(0);
			while (latch.getCount() > 0 || !ipsToDiscovery.isEmpty()) {
				while (!ipsToDiscovery.isEmpty()) {
					final String currentIp = ipsToDiscovery.pop();
					latch.countUp();
					taskExecutor.execute(new Runnable() {
						@Override
						public void run() {
							try {
								Element element = lldpDiscoverManager.discover(currentIp);
								if (element != null) {
									Set<String> currentPortMacs = new HashSet<String>();
									Element elementPersisted = null;
									Set<Port> notSavedPorts = new HashSet<Port>();
									for (Port port : element.getPortList()) {
										currentPortMacs.add(port.getMacAddress());
										// Save Or Update
										Port portPersisted = macToPort.get(port.getMacAddress());
										if (portPersisted != null) {
											merge(portPersisted, port);
											if (elementPersisted == null) {
												elementPersisted = idToElement.get(portPersisted.getIdElement());
											}
										} else {
											notSavedPorts.add(port);
										}
									}

									// Save or Update Element
									if (elementPersisted != null) {
										setDiscoveryFields(elementPersisted, new Date(), instance, "ExpiredTime",
												"LLDP");
										merge(elementPersisted, element);
										for (Port port : notSavedPorts) {
											port.setIdElement(elementPersisted.getIdElement());
											save(port);
										}

										for (Port port : idElementToPortSet.get(elementPersisted.getIdElement())) {
											if (!currentPortMacs.contains(port.getMacAddress())) {
												portsToRemove.add(port);
											}
										}
									} else {
										setDiscoveryFields(element, new Date(), instance, "ExpiredTime", "LLDP");
										saveCascade(element);
									}
								} else {
									ipsToRemove.add(currentIp);
								}
							} finally {
								latch.countDown();
							}
						}

					});
				}
				try {
					if (latch.getCount() > 0 || !ipsToDiscovery.isEmpty()) {
						logger.debug("Waiting... " + latch.getCount() + " discovery tasks running and "
								+ ipsToDiscovery.size() + " devices to discovery.");
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			taskExecutor.shutdown();
		}

		// Updating Topology
		Set<Port> portsChanged = new HashSet<Port>();
		portsChanged.addAll(removePorts(portsToRemove));
		portsChanged.addAll(linkElements());
		portsChanged.addAll(removeElements(ipsToRemove));

		if (!portsChanged.isEmpty()) {
			eventService.publish(SonarTopics.TOPIC_TOPOLOGY_LINKS_CHANGED, portsChanged);
		}
		logger.info("Discovery using 'Flooding' concluded!");
	}

	@Scheduled(fixedDelayString = "${topology.scoe.discovery.element.updateInterval:600000}", initialDelayString = "${topology.scoe.discovery.element.updateInterval:600000}")
	public void updateElements() throws InterruptedException {
		logger.info("Discovering network elements using 'Expiration' with interval "+elementUpdateInterval+" in millis...");

		final Set<String> ipsToRemove = new HashSet<String>();
		final Set<Port> portsToRemove = new HashSet<Port>();
		final Stack<String> ipsToDiscovery = new Stack<String>();
		final Map<UUID, Element> idToElement = new HashMap<UUID, Element>();
		for (Element element : topologyService.getElements()) {
			idToElement.put(element.getIdElement(), element);
			if (shouldUpdate(element)) {
				ipsToDiscovery.add(element.getManagementIPAddressList().iterator().next());
			}
		}

		if (!ipsToDiscovery.isEmpty()) {
			final Map<String, Port> macToPort = new HashMap<String, Port>();
			final Map<UUID, Set<Port>> idElementToPortSet = new HashMap<UUID, Set<Port>>();
			for (Port p : topologyService.getPorts()) {
				macToPort.put(p.getMacAddress(), p);

				if (!idElementToPortSet.containsKey(p.getIdElement())) {
					idElementToPortSet.put(p.getIdElement(), Sets.newHashSet(p));
				} else {
					idElementToPortSet.get(p.getIdElement()).add(p);
				}

			}

			final ExecutorService taskExecutor = Executors.newFixedThreadPool(4);
			final CountingLatch latch = new CountingLatch(0);
			while (latch.getCount() > 0 || !ipsToDiscovery.isEmpty()) {
				while (!ipsToDiscovery.isEmpty()) {
					final String currentIp = ipsToDiscovery.pop();
					latch.countUp();
					taskExecutor.execute(new Runnable() {
						@Override
						public void run() {
							try {
								Element element = lldpDiscoverManager.discover(currentIp);
								if (element != null) {
									Set<String> currentPortMacs = new HashSet<String>();
									Element elementPersisted = null;
									Set<Port> notSavedPorts = new HashSet<Port>();
									for (Port port : element.getPortList()) {
										currentPortMacs.add(port.getMacAddress());
										// Save Or Update
										Port portPersisted = macToPort.get(port.getMacAddress());
										if (portPersisted != null) {
											merge(portPersisted, port);
											if (elementPersisted == null) {
												elementPersisted = idToElement.get(portPersisted.getIdElement());
											}
										} else {
											notSavedPorts.add(port);
										}
									}

									// Save or Update Element
									if (elementPersisted != null) {
										setDiscoveryFields(elementPersisted, new Date(), instance, "ExpiredTime",
												"LLDP");
										merge(elementPersisted, element);
										for (Port port : notSavedPorts) {
											port.setIdElement(elementPersisted.getIdElement());
											save(port);
										}

										for (Port port : idElementToPortSet.get(elementPersisted.getIdElement())) {
											if (!currentPortMacs.contains(port.getMacAddress())) {
												portsToRemove.add(port);
											}
										}
									} else {
										setDiscoveryFields(element, new Date(), instance, "ExpiredTime", "LLDP");
										saveCascade(element);
									}
								} else {
									ipsToRemove.add(currentIp);
								}
							} finally {
								latch.countDown();
							}
						}
					});
				}

				try {
					if (latch.getCount() > 0 || !ipsToDiscovery.isEmpty()) {
						logger.debug("Waiting... " + latch.getCount() + " discovery tasks running and "
								+ ipsToDiscovery.size() + " devices to discovery.");
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			taskExecutor.shutdown();
		}

		// Updating Topology
		Set<Port> portsChanged = new HashSet<Port>();
		portsChanged.addAll(removePorts(portsToRemove));
		portsChanged.addAll(linkElements());
		portsChanged.addAll(removeElements(ipsToRemove));

		if (!portsChanged.isEmpty()) {
			eventService.publish(SonarTopics.TOPIC_TOPOLOGY_LINKS_CHANGED, portsChanged);
		}

		logger.info("Discovery using 'Expiration' concluded!");
	}

	private void discoverByIPAssignment(Element element) {
		logger.info("Discovering network elements using 'IP Assignment Event'...");

		Element discoveredElement = lldpDiscoverManager
				.discover(element.getManagementIPAddressList().iterator().next());
		if (discoveredElement != null) {
			// If Discovery fails, save the basic element representation.
			discoveredElement = element;
		}

		setDiscoveryFields(discoveredElement, new Date(), instance, "IP-ASSIGNMENT", "LLDP");

		Element savedElement = save(discoveredElement);
		eventService.publish(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_ADDED, savedElement);
		for (Port port : discoveredElement.getPortList()) {
			port.setIdElement(savedElement.getIdElement());
			save(port);
			eventService.publish(SonarTopics.TOPIC_TOPOLOGY_PORT_ADDED, port);
		}

		// Updating Topology
		Set<Port> portsChanged = new HashSet<Port>();
		portsChanged.addAll(linkElements());

		if (!portsChanged.isEmpty()) {
			eventService.publish(SonarTopics.TOPIC_TOPOLOGY_LINKS_CHANGED, portsChanged);
		}
		
		logger.info("Discovery using 'IP Assignment Event' concluded!");
	}

	/**
	 * Remove and Link
	 */

	private Set<Port> removePorts(Set<Port> portsToRemove) {
		logger.info("-> "+portsToRemove.size()+" ports removed!");
		Set<Port> portsChanged = new HashSet<Port>();

		Map<UUID, Port> idToPort = new HashMap<UUID, Port>();
		for (Port port : topologyService.getPorts()) {
			idToPort.put(port.getIdPort(), port);
		}

		for (Port port : portsToRemove) {
			if (port.getRemoteIdPort() != null) {
				if (idToPort.containsKey(port.getRemoteIdPort())) {
					Port remotePort = idToPort.get(port.getRemoteIdPort());
					remotePort.setRemoteIdPort(null);
					portsChanged.add(remotePort);
					update(remotePort);
				}
			}
			remove(port);
		}

		return portsChanged;
	}

	private Set<Port> removeElements(Set<String> ipsToRemove) {
		logger.info("-> "+ipsToRemove.size()+" ips to removed (if exists)!");

		Set<Port> portsChanged = new HashSet<Port>();

		Map<UUID, Element> elementsRemoved = new HashMap<UUID, Element>();

		for (Element element : topologyService.getElements()) {
			for (String ipAddress : element.getManagementIPAddressList()) {
				if (ipsToRemove.contains(ipAddress)) {
					elementsRemoved.put(element.getIdElement(), element);
				}
			}
		}

		for (Element element : elementsRemoved.values()) {
			// Remove References
			Set<Port> neighborPorts = topologyService.getPortsByRemoteIdElement(element.getIdElement());
			if (neighborPorts != null && !neighborPorts.isEmpty()) {
				for (Port port : neighborPorts) {
					// If neighbor will be removed too..
					if (elementsRemoved.containsKey(port.getIdElement())) {
						// do nothing yet
					} else {
						port.setRemoteHostname(null);
						port.setRemoteIdPort(null);
						port.setRemoteIfId(null);
						port.setRemoteIfName(null);
						port.setRemoteIpAddress(null);
						port.setRemoteMacAddress(null);
						port.setRemotePort(null);

						update(port);
					}
				}
			}

			// Remove Element
			removeCascade(element);
		}

		return portsChanged;
	}

	private Set<Port> linkElements() {
		Map<String, Port> macToPort = new HashMap<String, Port>();
		Set<Port> portsChanged = new HashSet<Port>();

		Set<Port> portSet = topologyService.getPorts();

		for (Port port : portSet) {
			macToPort.put(port.getMacAddress(), port);
		}

		for (Port port : portSet) {
			if (port.getRemoteMacAddress() != null) {
				Port remotePort = macToPort.get(port.getRemoteMacAddress());
				if (remotePort != null) {
					if (port.getRemoteIdPort() == null || !port.getRemoteIdPort().equals(remotePort.getIdPort())) {
						port.setRemoteIdPort(remotePort.getIdPort());
						portsChanged.add(port);
						update(port);
					}

					if (remotePort.getRemoteIdPort() == null
							|| remotePort.getRemoteIdPort().equals(port.getRemoteIdPort())) {
						remotePort.setRemoteIdPort(port.getIdPort());
						portsChanged.add(remotePort);
						update(remotePort);
					}
				} else {
					logger.warn("Unable to find port with macAddress: " + port.getRemoteMacAddress()
							+ " linked to port: " + ObjectUtils.toString(port));
				}
			}
		}

		logger.info("-> "+portsChanged.size()+" link ports changed!");

		return portsChanged;
	}

	/**
	 * Set Discovery Fields
	 */
	private Element setDiscoveryFields(Element element, Date lastDicoveredAt, String lastDicoveredBy,
			String lastDicoveredMethod, String lastDicoveredSource) {
		element.setLastDicoveredAt(lastDicoveredAt);
		element.setLastDicoveredBy(lastDicoveredBy);
		element.setLastDicoveredMethod(lastDicoveredMethod);
		element.setLastDicoveredSource(lastDicoveredSource);
		return element;
	}

	/**
	 * Save/Update/Delete/Merge
	 */
	private void merge(Port portPersisted, Port port) {
		boolean changed = false;

		if (port.getBandwidth() != null && (portPersisted.getBandwidth() == null
				&& !port.getBandwidth().equals(portPersisted.getBandwidth()))) {
			portPersisted.setBandwidth(port.getBandwidth());
			changed = true;
		}

		if (port.getIfId() != null
				&& (portPersisted.getIfId() == null && !port.getIfId().equals(portPersisted.getIfId()))) {
			portPersisted.setIfId(port.getIfId());
			changed = true;
		}

		if (port.getIfName() != null
				&& (portPersisted.getIfName() == null && !port.getIfName().equals(portPersisted.getIfName()))) {
			portPersisted.setIfName(port.getIfName());
			changed = true;
		}

		if (port.getIpAddress() != null && (portPersisted.getIpAddress() == null
				&& !port.getIpAddress().equals(portPersisted.getIpAddress()))) {
			portPersisted.setIpAddress(port.getIpAddress());
			changed = true;
		}

		if (port.getMacAddress() != null && (portPersisted.getMacAddress() == null
				&& !port.getMacAddress().equals(portPersisted.getMacAddress()))) {
			portPersisted.setMacAddress(port.getMacAddress());
			changed = true;
		}

		if (port.getRemoteIfId() != null && (portPersisted.getRemoteIfId() == null
				&& !port.getRemoteIfId().equals(portPersisted.getRemoteIfId()))) {
			portPersisted.setRemoteIfId(port.getRemoteIfId());
			changed = true;
		}

		if (port.getRemoteIpAddress() != null && (portPersisted.getRemoteIpAddress() == null
				&& !port.getRemoteIpAddress().equals(portPersisted.getRemoteIpAddress()))) {
			portPersisted.setRemoteIpAddress(port.getRemoteIpAddress());
			changed = true;
		}

		if (port.getRemoteMacAddress() != null && (portPersisted.getRemoteMacAddress() == null
				&& !port.getRemoteMacAddress().equals(portPersisted.getRemoteMacAddress()))) {
			portPersisted.setRemoteMacAddress(port.getRemoteMacAddress());
			changed = true;
		}

		if (port.getRemoteHostname() != null && (portPersisted.getRemoteHostname() == null
				&& !port.getRemoteHostname().equals(portPersisted.getRemoteHostname()))) {
			portPersisted.setRemoteHostname(port.getRemoteHostname());
			changed = true;
		}

		if (port.getRemoteIfName() != null && (portPersisted.getRemoteIfName() == null
				&& !port.getRemoteIfName().equals(portPersisted.getRemoteIfName()))) {
			portPersisted.setRemoteIfName(port.getRemoteIfName());
			changed = true;
		}

		if (changed) {
			this.update(portPersisted);
		}
	}

	private void merge(Element elementPersisted, Element element) {
		boolean changed = false;
		if (element.getClock() != null
				&& (elementPersisted.getClock() == null || !element.getClock().equals(elementPersisted.getClock()))) {
			elementPersisted.setClock(element.getClock());
			changed = true;
		}

		if (element.getCores() != null
				&& (elementPersisted.getCores() == null || !element.getCores().equals(elementPersisted.getCores()))) {
			elementPersisted.setCores(element.getCores());
			changed = true;
		}

		if (element.getCost() != null
				&& (elementPersisted.getCost() == null || !element.getCost().equals(elementPersisted.getCost()))) {
			elementPersisted.setCost(element.getCost());
			changed = true;
		}

		if (element.getDisk() != null
				&& (elementPersisted.getDisk() == null || !element.getDisk().equals(elementPersisted.getDisk()))) {
			elementPersisted.setDisk(element.getDisk());
			changed = true;
		}

		if (element.getEnergy() != null && (elementPersisted.getEnergy() == null
				|| !element.getEnergy().equals(elementPersisted.getEnergy()))) {
			elementPersisted.setEnergy(element.getEnergy());
			changed = true;
		}

		if (element.getMemory() != null && (elementPersisted.getMemory() == null
				|| !element.getMemory().equals(elementPersisted.getMemory()))) {
			elementPersisted.setMemory(element.getMemory());
			changed = true;
		}

		if (element.getName() != null
				&& (elementPersisted.getName() == null || !element.getName().equals(elementPersisted.getName()))) {
			elementPersisted.setName(element.getName());
			changed = true;
		}

		if (element.getTypeElement() != null && (elementPersisted.getTypeElement() == null
				|| !element.getTypeElement().equals(elementPersisted.getTypeElement()))) {
			elementPersisted.setTypeElement(element.getTypeElement());
			changed = true;
		}

		if (element.getManagementIPAddressList() != null && (elementPersisted.getManagementIPAddressList() == null
				|| !element.getManagementIPAddressList().equals(elementPersisted.getManagementIPAddressList()))) {
			elementPersisted.getManagementIPAddressList().addAll(element.getManagementIPAddressList());
			changed = true;
		}

		if (changed) {
			this.update(elementPersisted);
		}
	}

	private Element saveCascade(Element element) {
		setElementType(element);
		Element elementSaved = topologyService.save(element);
		for (Port port : element.getPortList()) {
			port.setIdElement(elementSaved.getIdElement());
			topologyService.save(port);
		}
		eventService.publish(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_ADDED, elementSaved);
		return element;
	}

	private void removeCascade(Element element) {
		topologyService.deletePortByIdElement(element.getIdElement());
		topologyService.deleteElementById(element.getIdElement());
		eventService.publish(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_REMOVED, element);
	}

	private void remove(Port port) {
		topologyService.delete(port);
		eventService.publish(SonarTopics.TOPIC_TOPOLOGY_PORT_REMOVED, port);
	}

	private Port save(Port port) {
		Port portSaved = topologyService.save(port);
		eventService.publish(SonarTopics.TOPIC_TOPOLOGY_PORT_ADDED, portSaved);
		return portSaved;
	}

	private Port update(Port port) {
		Port portSaved = topologyService.update(port);
		eventService.publish(SonarTopics.TOPIC_TOPOLOGY_PORT_CHANGED, portSaved);
		return portSaved;
	}

	private Element update(Element element) {
		Element elementSaved = topologyService.update(element);
		eventService.publish(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED, elementSaved);
		return elementSaved;
	}

	private Element save(Element element) {
		setElementType(element);
		Element elementSaved = topologyService.save(element);
		eventService.publish(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_ADDED, elementSaved);
		return elementSaved;
	}

	private void setElementType(Element element) {
		// Temporary solution: TODO fix this setElementType
		if (element.getName() != null && element.getName().startsWith("sw")) {
			element.setTypeElement(Element.TYPE_DEVICE);
		} else {
			if (element.getName() != null
					&& (element.getName().startsWith("nfvi") || element.getName().startsWith("nfci"))) {
				element.setTypeElement(Element.TYPE_SERVER);
			} else {
				element.setTypeElement(Element.TYPE_HOST);
			}
		}
	}

	/**
	 * Util
	 */
	private Boolean shouldUpdate(Element element) {
		Date lastUpdate = element.getLastDicoveredAt();

		Calendar whenUpdate = new GregorianCalendar();
		whenUpdate.setTime(lastUpdate);
		whenUpdate.add(Calendar.SECOND, elementUpdateInterval);

		Calendar now = new GregorianCalendar();
		now.setTime(new Date());

		return now.before(whenUpdate);
	}
}
