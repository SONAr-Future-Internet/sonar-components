package br.ufu.facom.mehar.sonar.collectors.topology.service;

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

import br.ufu.facom.mehar.sonar.client.ndb.service.TopologyService;
import br.ufu.facom.mehar.sonar.client.nem.action.NetworkEventAction;
import br.ufu.facom.mehar.sonar.client.nem.configuration.SonarTopics;
import br.ufu.facom.mehar.sonar.client.nem.service.EventService;
import br.ufu.facom.mehar.sonar.client.nim.element.service.ElementService;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;
import br.ufu.facom.mehar.sonar.core.model.topology.type.ElementType;
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
	private ElementService elementService;
	
	@Value("${sonar.server.local.ip.address:192.168.0.1}")
	private String serverLocalIpAddress;

	@Value("${topology.scoe.discovery.element.updateInterval:5000}")
	private Integer elementUpdateInterval;

	@Value("${topology.scoe.discovery.strategy.flooding.first:192.168.0.1}")
	private String floodingIntervalFirst;

	@Value("${topology.scoe.discovery.strategy.flooding.last:192.168.0.254}")
	private String floodingIntervalLast;
	
	@Value("${topology.scoe.discovery.strategy.timeout:2000}")
	private String timeoutInterval;
	
	@Value("${topology.scoe.discovery.strategy.join:10000}")
	private String joinInterval;
	
	private static final String METHOD_IP_ASSIGN = "IP Assign";
	private static final String METHOD_DFS = "Depth-First Search";
	private static final String METHOD_FLOODING = "Flooding";
	private static final String METHOD_TIMEOUT = "Timeout";
	private static final String METHOD_JOIN = "Join";
	
	private static final String SOURCE_LLDP = "LLDP";
	private static final String SOURCE_DHCP = "DHCP";
	
	/**
	 * Schedulers and Listerners
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void listenToEvents() throws InterruptedException {
		final DiscoveryService parent = this;
		eventService.subscribe(SonarTopics.TOPIC_TOPOLOGY_PORT_IP_ASSIGNED, new NetworkEventAction() {
			@Override
			public void handle(String event, String json) {
				synchronized (parent) {
					Port portEvent = ObjectUtils.toObject(json, Port.class);
					String ip = portEvent.getIpAddress();
					String mac = IPUtils.normalizeMAC(portEvent.getMacAddress());
	
					// find element and port using mac
					Pair<Element, Port> pairByMac = topologyService.getElementAndPortByPortMacAddress(mac);
					Element elementByMac = null;
					Port portByMac = null;
					if(pairByMac != null) {
						elementByMac = pairByMac.getFirst();
						portByMac = pairByMac.getSecond();
					}
	
					// find element and port using ip
					Pair<Element, Port> pairByIp = topologyService.getElementAndPortByPortIPAddress(ip);
					Element elementByIP = null;
					Port portByIPort = null;
					if(pairByIp != null) {
						elementByIP = pairByIp.getFirst();
						portByIPort = pairByIp.getSecond();
					}
					
	
					//if new element (not found element with ip or mac)
					if (elementByIP == null && elementByMac == null) {
						
						logger.info("Discovering network elements using 'IP Assignment Event'...");
						
						discover(ip, METHOD_IP_ASSIGN);
						
						logger.info("Discovery using 'IP Assignment Event' concluded!");
					} else {
						// element found with ip and mac at same time...
						if (elementByIP != null && elementByMac != null) {
							
							// and they are equals
							if (elementByMac.equals(elementByIP)) {
								// this is a reassignment of ip, so:
								// do nothing! Just wait for the normal discovery schedule.
							} else {
								// but they are not equals... we found a conflict!
								
								// remove ip on element found with the same ip
								elementByIP.getManagementIPAddressList().remove(ip);
								portByIPort.setIpAddress(ip);
	
								// remove old ip (if exists) configured on the port with the same mac
								if (portByMac.getIpAddress() != null) {
									if (elementByMac.getManagementIPAddressList() != null) {
										elementByMac.getManagementIPAddressList().remove(portByMac.getIpAddress());
									}
								}
								
								// set ip on port with related mac
								if (elementByMac.getManagementIPAddressList() == null) {
									elementByMac.setManagementIPAddressList(new HashSet<String>());
								}
								elementByMac.getManagementIPAddressList().add(ip);
								portByMac.setIpAddress(ip);
	
								// set 'update discovery info' of both elements
								setDiscoveryFields(elementByIP, new Date(), getInstanceDiscovery(), METHOD_IP_ASSIGN, SOURCE_DHCP);
								setDiscoveryFields(elementByMac, new Date(), getInstanceDiscovery(), METHOD_IP_ASSIGN, SOURCE_DHCP);
	
								// update both element with ip and element with mac (and also the related ports)
								update(elementByIP);
								update(elementByMac);
								update(portByIPort);
								update(portByMac);
							}
						} else {
							// if an element was found with the mac but not with ip (already created element) 
							if (elementByMac != null && elementByIP == null) {
								
								// remove old ip (if exists) configured on the port with the same mac
								if (portByMac.getIpAddress() != null) {
									if (elementByMac.getManagementIPAddressList() != null) {
										elementByMac.getManagementIPAddressList().remove(portByMac.getIpAddress());
									}
								}
								
								// set ip on port with related mac
								if (elementByMac.getManagementIPAddressList() == null) {
									elementByMac.setManagementIPAddressList(new HashSet<String>());
								}
								elementByMac.getManagementIPAddressList().add(ip);
								portByMac.setIpAddress(ip);
	
								// Set Discovery Fields
								setDiscoveryFields(elementByMac, new Date(), getInstanceDiscovery(), "IP-ASSIGNMENT", "DHCP");
	
								// Update (but not discovery)
								update(elementByMac);
								update(portByMac);
							} else {
								// if an element with the ip was found but not with the mac
								
								logger.info("Discovering network elements using 'IP Assignment Event'...");
								
								discover(ip, METHOD_IP_ASSIGN);
								
								logger.info("Discovery using 'IP Assignment Event' concluded!");
							}
						}
					}
				}
			}

		});
	}
	
	@Scheduled(fixedDelayString = "${topology.scoe.discovery.strategy.dfs.interval:600000}")
	public void discoveryDFS() throws InterruptedException {
		synchronized (this) {
			logger.info("Discovering network elements using 'Depth-First Search' with local server as root...");
			
			discover(serverLocalIpAddress, METHOD_DFS);
		
			logger.info("Discovery using 'Depth-First Search' concluded!");
		}
	}

	@Scheduled(fixedDelayString = "${topology.scoe.discovery.strategy.flooding.interval:60000000}", initialDelayString = "${topology.scoe.discovery.strategy.flooding.interval:60000000}")
	public void discoveryFlooding() throws InterruptedException {
		synchronized (this) {
			final Stack<String> ipsToDiscovery = new Stack<String>();
			String ip = floodingIntervalFirst;
	
			while (!ip.equals(floodingIntervalLast)) {
				ipsToDiscovery.add(ip);
				ip = IPUtils.nextIP(ip);
			}
	
			if (!ipsToDiscovery.isEmpty()) {
				
				logger.info("Discovering network elements using 'Flooding' with interval "+floodingIntervalFirst+"-"+floodingIntervalLast+"...");
				
				discover(ipsToDiscovery, METHOD_FLOODING);
				
				logger.info("Discovery using 'Flooding' concluded!");
			}
		}
	}

	@Scheduled(fixedDelayString = "${topology.scoe.discovery.strategy.timeout:2000}", initialDelayString = "${topology.scoe.discovery.strategy.timeout:10000}")
	public void updateElements() throws InterruptedException {
		synchronized (this) {
			final Stack<String> ipsToDiscovery = new Stack<String>();
			for (Element element : topologyService.getElements()) {
				if (shouldUpdate(element)) {
					ipsToDiscovery.add(element.getManagementIPAddressList().iterator().next());
				}
			}
	
			if (!ipsToDiscovery.isEmpty()) {
				logger.info("Discovering network elements using 'Timeout' with interval "+timeoutInterval+" in millis...");
				
				discover(ipsToDiscovery, METHOD_TIMEOUT);
	
				logger.info("Discovery using 'Timeout' concluded!");
			}
		}
	}
	
	@Scheduled(fixedDelayString = "${topology.scoe.discovery.strategy.join:5000}", initialDelayString = "${topology.scoe.discovery.strategy.join:5000}")
	public void verifyIsolated() throws InterruptedException {
		synchronized (this) {
			final Stack<String> ipsToDiscovery = new Stack<String>();
			for (Element element : topologyService.getElements()) {
				if(getNeighbors(element).isEmpty()) {
					ipsToDiscovery.add(element.getManagementIPAddressList().iterator().next());
				}
			}
	
			if (!ipsToDiscovery.isEmpty()) {
				logger.info("Discovering network elements using 'Join' with interval "+joinInterval+" in millis...");
				
				discover(ipsToDiscovery, METHOD_TIMEOUT);
	
				logger.info("Discovery using 'Timeout' concluded!");
			}
		}
	}


	/**
	 * CRUD
	 */
	private Element saveCascade(Element element) {
		logger.info("New Element saved: "+ObjectUtils.toString(element));
		setElementType(element);
		Element elementSaved = topologyService.save(element);
		for (Port port : element.getPortList()) {
			port.setIdElement(elementSaved.getIdElement());
			topologyService.save(port);
		}
		eventService.publish(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_ADDED, elementSaved);
		return element;
	}
	
	private Set<Port> removeCascade(Element persistedElement) {
		Set<Port> linkPortsChanged = new HashSet<Port>();
		
		Set<Port> persistedPorts = getPortsByIdElement(persistedElement.getIdElement());
		if( persistedPorts != null && !persistedPorts.isEmpty()) {
			for(Port portRemoved : persistedPorts) {
				if(portRemoved.getRemoteIpAddress() != null || portRemoved.getRemoteMacAddress() != null) {
					//Port linked being removed
					linkPortsChanged.add(portRemoved);
				}
				remove(portRemoved);
			}
		}
		topologyService.deleteElementById(persistedElement.getIdElement());
		eventService.publish(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_REMOVED, persistedElement);
		
		return linkPortsChanged;
	}
	
	private Set<Port> mergeCascade(Element persistedElement, Element discoveredElement) {
		Set<Port> linkPortsChanged = new HashSet<Port>();
		boolean elementChanged = false;
		
		//Verify all attributes
		if (discoveredElement.getClock() != null
				&& (persistedElement.getClock() == null || !discoveredElement.getClock().equals(persistedElement.getClock()))) {
			persistedElement.setClock(discoveredElement.getClock());
			elementChanged = true;
		}
		if (discoveredElement.getCores() != null
				&& (persistedElement.getCores() == null || !discoveredElement.getCores().equals(persistedElement.getCores()))) {
			persistedElement.setCores(discoveredElement.getCores());
			elementChanged = true;
		}
		if (discoveredElement.getCost() != null
				&& (persistedElement.getCost() == null || !discoveredElement.getCost().equals(persistedElement.getCost()))) {
			persistedElement.setCost(discoveredElement.getCost());
			elementChanged = true;
		}
		if (discoveredElement.getDisk() != null
				&& (persistedElement.getDisk() == null || !discoveredElement.getDisk().equals(persistedElement.getDisk()))) {
			persistedElement.setDisk(discoveredElement.getDisk());
			elementChanged = true;
		}
		if (discoveredElement.getEnergy() != null && (persistedElement.getEnergy() == null
				|| !discoveredElement.getEnergy().equals(persistedElement.getEnergy()))) {
			persistedElement.setEnergy(discoveredElement.getEnergy());
			elementChanged = true;
		}
		if (discoveredElement.getMemory() != null && (persistedElement.getMemory() == null
				|| !discoveredElement.getMemory().equals(persistedElement.getMemory()))) {
			persistedElement.setMemory(discoveredElement.getMemory());
			elementChanged = true;
		}
		if (discoveredElement.getName() != null
				&& (persistedElement.getName() == null || !discoveredElement.getName().equals(persistedElement.getName()))) {
			persistedElement.setName(discoveredElement.getName());
			elementChanged = true;
		}
		if (discoveredElement.getTypeElement() != null && (persistedElement.getTypeElement() == null
				|| !discoveredElement.getTypeElement().equals(persistedElement.getTypeElement()))) {
			persistedElement.setTypeElement(discoveredElement.getTypeElement());
			elementChanged = true;
		}
		if (discoveredElement.getManagementIPAddressList() != null && (persistedElement.getManagementIPAddressList() == null
				|| !persistedElement.getManagementIPAddressList().containsAll(discoveredElement.getManagementIPAddressList()))) {
			persistedElement.getManagementIPAddressList().addAll(discoveredElement.getManagementIPAddressList());
			elementChanged = true;
		}
		
		Set<Port> persistedPorts = getPortsByIdElement(persistedElement.getIdElement());
		if( persistedPorts == null ) {
			persistedPorts = new HashSet<Port>();
		}
		
		Map<String, Port> mapPersistedPort = new HashMap<String, Port>();
		Set<Port> portsRemoved = new HashSet<Port>();
		for(Port persistedPort : persistedPorts) {
			mapPersistedPort.put(persistedPort.getMacAddress(), persistedPort);
			portsRemoved.add(persistedPort);
		}
		
		for(Port discoveredPort : discoveredElement.getPortList()) {
			// update port
			if(mapPersistedPort.containsKey(discoveredPort.getMacAddress())) {
				Port persistedPort = mapPersistedPort.get(discoveredPort.getMacAddress());
				
				portsRemoved.remove(persistedPort);
				
				boolean portChanged = false;
				if (discoveredPort.getBandwidth() != null && (persistedPort.getBandwidth() == null
						&& !discoveredPort.getBandwidth().equals(persistedPort.getBandwidth()))) {
					persistedPort.setBandwidth(discoveredPort.getBandwidth());
					portChanged = true;
				}
				if (discoveredPort.getIfId() != null
						&& (persistedPort.getIfId() == null && !discoveredPort.getIfId().equals(persistedPort.getIfId()))) {
					persistedPort.setIfId(discoveredPort.getIfId());
					portChanged = true;
				}
				if (discoveredPort.getIfName() != null
						&& (persistedPort.getIfName() == null && !discoveredPort.getIfName().equals(persistedPort.getIfName()))) {
					persistedPort.setIfName(discoveredPort.getIfName());
					portChanged = true;
				}
				if (discoveredPort.getIpAddress() != null && (persistedPort.getIpAddress() == null
						&& !discoveredPort.getIpAddress().equals(persistedPort.getIpAddress()))) {
					persistedPort.setIpAddress(discoveredPort.getIpAddress());
					portChanged = true;
				}
				if (discoveredPort.getMacAddress() != null && (persistedPort.getMacAddress() == null
						&& !discoveredPort.getMacAddress().equals(persistedPort.getMacAddress()))) {
					persistedPort.setMacAddress(discoveredPort.getMacAddress());
					portChanged = true;
				}
				
				if (discoveredPort.getRemoteIfId() == null || (discoveredPort.getRemoteIfId() != null && (persistedPort.getRemoteIfId() == null
						&& !discoveredPort.getRemoteIfId().equals(persistedPort.getRemoteIfId())))) {
					persistedPort.setRemoteIfId(discoveredPort.getRemoteIfId());
					portChanged = true;
				}
				if (discoveredPort.getRemoteIpAddress() == null || (discoveredPort.getRemoteIpAddress() != null && (persistedPort.getRemoteIpAddress() == null
						&& !discoveredPort.getRemoteIpAddress().equals(persistedPort.getRemoteIpAddress())))) {
					persistedPort.setRemoteIpAddress(discoveredPort.getRemoteIpAddress());
					portChanged = true;
				}
				if (discoveredPort.getRemoteMacAddress() == null || (discoveredPort.getRemoteMacAddress() != null && (persistedPort.getRemoteMacAddress() == null
						&& !discoveredPort.getRemoteMacAddress().equals(persistedPort.getRemoteMacAddress())))) {
					persistedPort.setRemoteMacAddress(discoveredPort.getRemoteMacAddress());
					portChanged = true;
				}
				if (discoveredPort.getRemoteHostname() == null || (discoveredPort.getRemoteHostname() != null && (persistedPort.getRemoteHostname() == null
						&& !discoveredPort.getRemoteHostname().equals(persistedPort.getRemoteHostname())))) {
					persistedPort.setRemoteHostname(discoveredPort.getRemoteHostname());
					portChanged = true;
				}
				if (discoveredPort.getRemoteIfName() == null || (discoveredPort.getRemoteIfName() != null && (persistedPort.getRemoteIfName() == null
						&& !discoveredPort.getRemoteIfName().equals(persistedPort.getRemoteIfName())))) {
					persistedPort.setRemoteIfName(discoveredPort.getRemoteIfName());
					portChanged = true;
				}
				
				if (portChanged) {
					update(persistedPort);
				}
			}else {
				discoveredPort.setIdElement(persistedElement.getIdElement());
				save(discoveredPort);
			}
		}
		
		for(Port portRemoved : portsRemoved) {
			if(portRemoved.getRemoteIpAddress() != null || portRemoved.getRemoteMacAddress() != null) {
				//Port linked being removed
				linkPortsChanged.add(portRemoved);
			}
			remove(portRemoved);
		}
		
		if(elementChanged) {
			update(persistedElement);
		}else {
			//update to set timeout, but without events
			topologyService.update(persistedElement);
		}
		
		return linkPortsChanged;
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
	
	private Port save(Port port) {
		Port portSaved = topologyService.save(port);
		eventService.publish(SonarTopics.TOPIC_TOPOLOGY_PORT_ADDED, portSaved);
		return portSaved;
	}

	private void remove(Port port) {
		topologyService.delete(port);
		eventService.publish(SonarTopics.TOPIC_TOPOLOGY_PORT_REMOVED, port);
	}
	
	/**
	 * Query data from cache or database
	 */
	private Set<Port> getPortsByIdElement(UUID idElement) {
		return topologyService.getPortsByIdElement(idElement); 
	}
	private Element getElementByIP(String currentIp) {
		return topologyService.getElementByIPAddress(currentIp);
	}
	
	private Element getElementById(UUID idElement) {
		return topologyService.getElementById(idElement);
	}

	private Port getPortByMac(String macAddress) {
		return topologyService.getPortByMacAddress(macAddress);
	}

	/**
	 * Main Logic
	 */

	private void discover(final String ip, final String method) {
		final Stack<String> ipsToDiscovery = new Stack<String>();
		ipsToDiscovery.add(ip);
		this.discover(ipsToDiscovery, method);
	}
	
	private void discover(final Stack<String> ipsToDiscovery, final String method) {
		final ExecutorService taskExecutor = Executors.newFixedThreadPool(4);
		final CountingLatch latch = new CountingLatch(0);
		final Set<Port> linkPortsChanged = new HashSet<Port>();
		final Set<String> ipsDiscovered = new HashSet<String>();
		
		// while there is ip's to discovery or there is a discovery running
		while (latch.getCount() > 0 || !ipsToDiscovery.isEmpty()) {
			if (!ipsToDiscovery.isEmpty()) {
				
				// get current IP of stack, countUp the task and add to set of already discovery ips
				final String currentIp = ipsToDiscovery.pop();
				ipsDiscovered.add(currentIp);
				latch.countUp();
				
				// run discovery task
				taskExecutor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							// discover current ip
							Element discoveredElement = elementService.discover(currentIp);
							
							// find element already persisted
							Element persistedElement = getElementByIP(currentIp);
							
							// if discovery worked
							if(discoveredElement != null) {
								for(Port port : discoveredElement.getPortList()) {
									Pair<Element, Port> pairElementAndPort = topologyService.getElementAndPortByPortMacAddress(port.getMacAddress());
									if(pairElementAndPort != null) {
										persistedElement = pairElementAndPort.getFirst();
										break;
									}
								}
								
								// and element is already persisted
								if(persistedElement != null) {
									// set update data
									setDiscoveryFields(persistedElement, new Date(), getInstanceDiscovery(), method, SOURCE_LLDP);
									
									linkPortsChanged.addAll( mergeCascade(persistedElement, discoveredElement) );
								}else {
									// set update data
									setDiscoveryFields(discoveredElement, new Date(), getInstanceDiscovery(), method, SOURCE_LLDP);
									
									// if not persisted
									saveCascade(discoveredElement);
								}
								
								//add neighbors considering the method
								switch(method) {
									case METHOD_FLOODING: 
										break;//don't add neighbors
									case METHOD_TIMEOUT: 
										for(String ipNeighbor : getNeighbors(discoveredElement)) {
											if(!ipsDiscovered.contains(ipNeighbor) && !ipsToDiscovery.contains(ipNeighbor)) {
												Element neighborPersisted = getElementByIP(ipNeighbor);
												if(neighborPersisted == null) {
													ipsToDiscovery.add(ipNeighbor);
												}
											}
										}
										break;
									case METHOD_DFS:
										for(String ipNeighbor : getNeighbors(discoveredElement)) {
											if(!ipsDiscovered.contains(ipNeighbor) && !ipsToDiscovery.contains(ipNeighbor)) {
												ipsToDiscovery.add(ipNeighbor);
											}
										}
										break;
									case METHOD_IP_ASSIGN:
										for(String ipNeighbor : getNeighbors(discoveredElement)) {
											if(!ipsDiscovered.contains(ipNeighbor) && !ipsToDiscovery.contains(ipNeighbor)) {
												Element neighborPersisted = getElementByIP(ipNeighbor);
												if(neighborPersisted == null) {
													ipsToDiscovery.add(ipNeighbor);
												}
											}
										}
										break;
									case METHOD_JOIN:
										for(String ipNeighbor : getNeighbors(discoveredElement)) {
											if(!ipsDiscovered.contains(ipNeighbor) && !ipsToDiscovery.contains(ipNeighbor)) {
												Element neighborPersisted = getElementByIP(ipNeighbor);
												if(neighborPersisted == null) {
													ipsToDiscovery.add(ipNeighbor);
												}
											}
										}
										break;
								}
							}else {
								// if discovery not worked...
								// but there is a persisted element 
								if(persistedElement != null) {
									linkPortsChanged.addAll( removeCascade(persistedElement) );
								}else {
									// and there isn't a persisted element, so...
									// do nothing!
								}
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
		
//		logger.info("Links Changed Before Link using method "+method+" : "+ ObjectUtils.toString(linkPortsChanged));
		
		// Updating Topology
		linkPortsChanged.addAll(linkElements());
		
//		logger.info("Links Changed After Link using method "+method+"... "+ ObjectUtils.toString(linkPortsChanged));
		
		if (!linkPortsChanged.isEmpty()) {
			eventService.publish(SonarTopics.TOPIC_TOPOLOGY_LINKS_CHANGED, linkPortsChanged);
		}

		taskExecutor.shutdown();
	}
	
	private Set<Port> linkElements() {
		Map<String, Port> macToPort = new HashMap<String, Port>();
		Map<UUID, Port> idToPort = new HashMap<UUID, Port>();
		Set<Port> portsChanged = new HashSet<Port>();

		Set<Port> portSet = topologyService.getPorts();

		for (Port port : portSet) {
			macToPort.put(port.getMacAddress(), port);
			idToPort.put(port.getIdPort(), port);
		}

		for (Port port : portSet) {
			if (port.getRemoteMacAddress() != null) {
				Port remotePort = macToPort.get(port.getRemoteMacAddress());
				if (remotePort != null) {
					if (port.getRemoteIdPort() == null || !port.getRemoteIdPort().equals(remotePort.getIdPort())) {
//						logger.info("Port "+ObjectUtils.fromObject(port));
						port.setRemoteIdPort(remotePort.getIdPort());
						portsChanged.add(port);
						update(port);
					}

					if (remotePort.getRemoteIdPort() == null
							|| remotePort.getRemoteIdPort().equals(port.getRemoteIdPort())) {
//						logger.info("Remote "+ObjectUtils.fromObject(remotePort));
						remotePort.setRemoteIdPort(port.getIdPort());
						portsChanged.add(remotePort);
						update(remotePort);
					}
				} else {
					logger.debug("Unable to find port with macAddress: " + port.getRemoteMacAddress()
							+ " linked to port: " + ObjectUtils.toString(port));
				}
			}else {
				if(port.getRemoteIdPort() != null) {
					Port remotePort = idToPort.get(port.getRemoteIdPort());
					if (remotePort != null && remotePort.getRemoteMacAddress() != null && remotePort.getRemoteMacAddress().equals(port.getMacAddress())) {
						//Do nothing yet... wait for a new discovery
//						Element element = getElementById(port.getIdElement());
//						Element remoteElement = getElementById(remotePort.getIdElement());
//						
//						logger.info("Last Discovered Element: "+element.getLastDicoveredAt());
//						logger.info("Last Discovered Remote Element: "+remoteElement.getLastDicoveredAt());
//						
//						if(element.getLastDicoveredAt().compareTo(remoteElement.getLastDicoveredAt()) > 0) {
//							logger.info("Port "+ObjectUtils.fromObject(port)+" Remote "+ObjectUtils.fromObject(remotePort));
//							//cleanup link
//							remotePort.setRemoteHostname(null);
//							remotePort.setRemoteIdPort(null);
//							remotePort.setRemoteIfId(null);
//							remotePort.setRemoteIfName(null);
//							remotePort.setRemoteIpAddress(null);
//							remotePort.setRemoteMacAddress(null);
//							portsChanged.add(remotePort);
//							update(remotePort);
//							
//							port.setRemoteIdPort(null);						
//							portsChanged.add(port);
//							update(port);
//						}else {
//							//do nothing... just keep on waiting for element update
//						}
					}else {
//						logger.info("Port "+ObjectUtils.fromObject(port)+" Remote NULL");
						port.setRemoteIdPort(null);
						portsChanged.add(port);
						update(port);
					}
				}
			}
		}
		
		if(!portsChanged.isEmpty()) {
			logger.info("-> "+portsChanged.size()+" link ports changed!");
		}

		return portsChanged;
	}
	
	private Set<String> getNeighbors(Element element) {
		Set<String> neighbors = new HashSet<String>();
		if(element.getPortList() == null || element.getPortList().isEmpty()) {
			element.setPortList(getPortsByIdElement(element.getIdElement()));
		}
		
		for(Port port : element.getPortList()) {
			if(port.getRemoteIpAddress() != null) {
				neighbors.add(port.getRemoteIpAddress());
			}else {
				if(port.getRemoteMacAddress() != null) {
					Port remotePort = getPortByMac(port.getMacAddress());
					if(remotePort != null) {
						if(remotePort.getIpAddress() != null) {
							neighbors.add(remotePort.getIpAddress());
						}else {
							Element remoteElement = getElementById(remotePort.getIdElement());
							if(remoteElement != null) {
								neighbors.add(remoteElement.getManagementIPAddressList().iterator().next());
							}
						}
					}
				}
			}
		}
		return neighbors;
	}
	
	/**
	 * Util
	 */
	private String getInstanceDiscovery() {
		return "tscoe-"+serverLocalIpAddress;
	}
	
	private Boolean shouldUpdate(Element element) {
		Date lastUpdate = element.getLastDicoveredAt();

		Calendar whenUpdate = new GregorianCalendar();
		whenUpdate.setTime(lastUpdate);
		whenUpdate.add(Calendar.MILLISECOND, elementUpdateInterval);

		Calendar now = new GregorianCalendar();
		now.setTime(new Date());

		return !now.before(whenUpdate);
	}
	
	private void setElementType(Element element) {
		// Temporary solution: TODO fix this setElementType
		if (element.getName() != null && element.getName().startsWith("sw")) {
			element.setTypeElement(ElementType.DEVICE);
		} else {
			if (element.getName() != null
					&& (element.getName().startsWith("sonar-server") || element.getName().startsWith("nfvi") || element.getName().startsWith("nfci"))) {
				element.setTypeElement(ElementType.SERVER);
			} else {
				element.setTypeElement(ElementType.DEVICE);
			}
		}
	}
	
	private Element setDiscoveryFields(Element element, Date lastDicoveredAt, String lastDicoveredBy,
			String lastDicoveredMethod, String lastDicoveredSource) {
		element.setLastDicoveredAt(lastDicoveredAt);
		element.setLastDicoveredBy(lastDicoveredBy);
		element.setLastDicoveredMethod(lastDicoveredMethod);
		element.setLastDicoveredSource(lastDicoveredSource);
		return element;
	}
}
