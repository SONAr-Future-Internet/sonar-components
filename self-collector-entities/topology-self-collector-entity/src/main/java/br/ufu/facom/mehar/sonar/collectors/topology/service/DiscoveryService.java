package br.ufu.facom.mehar.sonar.collectors.topology.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import br.ufu.facom.mehar.sonar.client.ndb.service.TopologyDataService;
import br.ufu.facom.mehar.sonar.client.nem.action.NetworkEventAction;
import br.ufu.facom.mehar.sonar.client.nem.configuration.SonarTopics;
import br.ufu.facom.mehar.sonar.client.nem.service.EventService;
import br.ufu.facom.mehar.sonar.client.nim.element.service.ElementService;
import br.ufu.facom.mehar.sonar.core.model.event.IPAssignmentEvent;
import br.ufu.facom.mehar.sonar.core.model.topology.Domain;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;
import br.ufu.facom.mehar.sonar.core.model.topology.Link;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;
import br.ufu.facom.mehar.sonar.core.model.topology.type.ElementState;
import br.ufu.facom.mehar.sonar.core.model.topology.type.ElementType;
import br.ufu.facom.mehar.sonar.core.model.topology.type.LinkEvent;
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
	private TopologyDataService topologyService;

	@Autowired
	private ElementService elementService;
	
	/*
	 * General Information
	 */
	@Value("${sonar.server.local.ip.address:192.168.0.1}")
	private String serverLocalIpAddress;

	/*
	 * Discovery configuration
	 */
	@Value("${topology.scoe.discovery.expiration.timeout:5000}")
	private Integer discoveryExpirationTimeout;
	@Value("${topology.scoe.discovery.waiting.timeout:120000}")
	private Integer waitingStateTimeout;
	@Value("${topology.scoe.discovery.disconnected.maxAttempts:20}")
	private Integer maxAttemptsBeforeDelete;

	/*
	 * Fast-Boot configuration
	 */
	@Value("${topology.scoe.discovery.fast-boot.minDevices:1}")
	private Integer fastBootMinDevices;
	@Value("${topology.scoe.discovery.fast-boot.maxAttempts:10}")
	private Integer fastBootMaxAttempts;
	@Value("${topology.scoe.discovery.fast-boot.waitTimeBeforeAttempting:0}")
	private Integer fastBootWaitTimeBeforeAttempting;
	@Value("${topology.scoe.discovery.fast-boot.waitTimeBetweenAttempts:2000}")
	private Integer fastBootWaitTimeAttempts;
	//Merge Strategy
	@Value("${topology.scoe.discovery.fast-boot.persistence.strategy:truncate}")
	private String fastBootPersistenceStrategy;
	private static final String BOOTING_STRATEGY_MERGE = "merge";
	private static final String BOOTING_STRATEGY_TRUNCATE = "truncate";
	
	/*
	 * Discovery Source Constants
	 */
	private static final String SOURCE_CONTROLLER = "CONTROLLER";
	private static final String SOURCE_DISCOVERY = "DISCOVERY";
	private static final String SOURCE_DHCP = "DHCP";

	/*
	 * Discovery Methods Constants
	 */
	private static final String METHOD_IP_ASSIGN = "IP_ASSIGNMENT";
	private static final String METHOD_BOOT = "BOOT";
	private static final String METHOD_EXPIRATION = "EXPIRATION";
	private static final String METHOD_CONTROLLER = "QUERY_CONTROLLER";

	/*
	 * Discovery State (used for synchronization)
	 */
	private static final String DISCOVERY_STATE_RUNNING="GREEN";
	private static final String DISCOVERING_STATE_STOPPED="RED";
	private volatile String discovery_state = DISCOVERY_STATE_RUNNING;

	/*
	 * Network State (changed after bootstrapping procedures)
	 */
	private static final String NETWORK_STATE_BOOTING = "BOOTING";
	private static final String NETWORK_STATE_RUNNING = "RUNNING";
	private volatile String network_state = NETWORK_STATE_BOOTING;
	
	/*
	 * Caching of IP assignment to Ports (used for discovery)
	 */
	private static final Map<String,String> macToIpCache = Collections.synchronizedMap(new HashMap<String, String>());
	
	/*
	 * Caching IDs 
	 */
	private static final Map<String,UUID> macToIdPortCache = Collections.synchronizedMap(new HashMap<String, UUID>());
	private static final Map<String,UUID> ipAndPortNameToIdPortCache = Collections.synchronizedMap(new HashMap<String, UUID>());
	private static final Map<String,UUID> ipToIdElementCache = Collections.synchronizedMap(new HashMap<String, UUID>());
	
	/**
	 * Schedulers and Listerners
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void boot() throws InterruptedException {
		//synchronized (this) {
			//Run 3 Stages Boot
			logger.info("Starting Network!");
			
			//Stage 1 - Discovery
			if(fastBootWaitTimeBeforeAttempting > 0) {
				Thread.sleep(fastBootWaitTimeBeforeAttempting);
			}
			
			eventService.publish(SonarTopics.TOPIC_BOOT_START_DISCOVERY_STAGE, "");
			Collection<Element> discoveredElements = bootDiscovery();
			int attemptCount = 1;
			while(discoveredElements.size() <= fastBootMinDevices && attemptCount < fastBootMaxAttempts) {
				attemptCount++;
				Thread.sleep(fastBootWaitTimeAttempts);
				//eventService.publish(SonarTopics.TOPIC_BOOT_START_DISCOVERY_STAGE, "");
				discoveredElements = bootDiscovery();
				logger.info("Network started!");
			}
			eventService.publish(SonarTopics.TOPIC_BOOT_FINISH_DISCOVERY_STAGE, "");
			
			//if just server was discovered... move on!
			if(discoveredElements.size() <= 1) {
				for(Element element : discoveredElements) {
					saveCascade(element);
					eventService.publish(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_DISCOVERED, element);
				}
				network_state = NETWORK_STATE_RUNNING;
			}else {
				// call SCE to process other stages...
				Domain bootDomain = new Domain();
				bootDomain.setElementList(new HashSet<Element>(discoveredElements));
				eventService.publish(SonarTopics.TOPIC_SCE_CALL_BOOT,bootDomain );
				//... and waits for the result
				eventService.subscribe(SonarTopics.TOPIC_SCE_CALLBACK_BOOT, new NetworkEventAction() {
					@Override
					public void handle(String event, String json) {
						Domain domain = ObjectUtils.toObject(json,Domain.class);
						
						if(BOOTING_STRATEGY_TRUNCATE.equals(fastBootPersistenceStrategy)) {
							topologyService.deleteDomains();
							topologyService.deleteElements();
							topologyService.deletePorts();
							
							for(Element element : domain.getElementList()) {
								saveCascade(element);
								if(element.getTypeElement().equals(ElementType.SERVER)) {
									eventService.publish(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_DISCOVERED, element);
								}else {
									eventService.publish(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_CONFIGURED, element);
								}
							}
							
							macToIdPortCache.clear();
							ipToIdElementCache.clear();
							for(Element element : domain.getElementList()) {
								indexIDs(element);
							}
							
						}else {
							if(BOOTING_STRATEGY_MERGE.equals(fastBootPersistenceStrategy)) {
								throw new NotImplementedException("Method 'merge' of persistence strategy not implemented yet!");
							}
						}
						
						network_state = NETWORK_STATE_RUNNING;
						logger.info("Network started!");
					}

				});
			}
		//}
	}
	
	//UTIL
	private void indexIDs(Element element) {
		for(String ip : element.getIpAddressList()) {
			ipToIdElementCache.put(ip, element.getIdElement());
			if(element.getPortList() != null) {
				for(Port port : element.getPortList()) {
					ipAndPortNameToIdPortCache.put(ip+":"+port.getPortName(), port.getIdPort());
				}
			}
		}
		if(element.getPortList() != null) {
			for(Port port : element.getPortList()) {
				macToIdPortCache.put(port.getMacAddress(), port.getIdPort());
			}
		}
	}
	
//	@EventListener(ApplicationReadyEvent.class)
//	public void listenToEvents() throws InterruptedException {
//		//final Object parent = this;
//		eventService.subscribe(SonarTopics.TOPIC_DHCP_IP_ASSIGNED, new NetworkEventAction() {
//			@Override
//			public void handle(String event, String json) {
//				IPAssignmentEvent ipAssignmentEvent = ObjectUtils.toObject(json, IPAssignmentEvent.class);
//				String ip = ipAssignmentEvent.getIp();
//				String mac = IPUtils.normalizeMAC(ipAssignmentEvent.getMac());
//				
//				macToIpCache.put(mac,ip);
//					
//				if(NETWORK_STATE_RUNNING.equals(network_state)) {	
//					//synchronized (parent) {
//						// find element and port using mac
//						Pair<Element, Port> pairByMac = topologyService.getElementAndPortByPortMacAddress(mac);
//						Element elementByMac = null;
//						Port portByMac = null;
//						if (pairByMac != null) {
//							elementByMac = pairByMac.getFirst();
//							portByMac = pairByMac.getSecond();
//						}
//		
//						// find element and port using ip
//						Pair<Element, Port> pairByIp = topologyService.getElementAndPortByPortIPAddress(ip);
//						Element elementByIP = null;
//						Port portByIP = null;
//						if (pairByIp != null) {
//							elementByIP = pairByIp.getFirst();
//							portByIP = pairByIp.getSecond();
//						}
//		
//						// if new element (not found element with ip or mac)
//						if (elementByIP == null && elementByMac == null) {
//							
//							// create basic representation of element
//							Element element = buildBasicElementByIpAndMac(ip, mac, Boolean.TRUE);
//							element.setState(ElementState.IP_ASSIGNED_BY_DHCP);
//							
//							// set discovery fields
//							setDiscoveryFields(element, new Date(), getInstanceDiscovery(), METHOD_IP_ASSIGN, SOURCE_DHCP);
//							
//							// save element
//							Element elementUpdated = saveCascade(element);
//							
//							// send saved state event (initial state before finding the element in topology)
//							eventService.publish(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_IP_ASSIGNED_BY_DHCP, elementUpdated);
//		
//							// try to discovery it (it may be not accessible)
//							Collection<Element> newElement = discover(ip, Boolean.FALSE, Boolean.TRUE);
//							//TODO Continue here
//						} else {
//							// element found with ip and mac at same time...
//							if (elementByIP != null && elementByMac != null) {
//		
//								// and they are equals
//								if (elementByMac.equals(elementByIP)) {
//									// this is a reassignment of ip, so:
//									// do nothing! Just wait for the normal discovery schedule.
//								} else {
//									// but they are not equals... we found a conflict!
//		
//									// remove ip on element found with the same ip
//									elementByIP.getIpAddressList().remove(ip);
//									portByIP.setIpAddress(ip);
//		
//									// remove old ip (if exists) configured on the port with the same mac
//									if (portByMac.getIpAddress() != null) {
//										if (elementByMac.getIpAddressList() != null) {
//											elementByMac.getIpAddressList().remove(portByMac.getIpAddress());
//										}
//									}
//		
//									// set ip on port with related mac
//									if (elementByMac.getIpAddressList() == null) {
//										elementByMac.setIpAddressList(new HashSet<String>());
//									}
//									elementByMac.getIpAddressList().add(ip);
//									portByMac.setIpAddress(ip);
//		
//									// update both element with ip and element with mac (and also the related ports)
//									update(elementByIP);
//									update(elementByMac);
//									update(portByIP);
//									update(portByMac);
//								}
//							} else {
//								// if an element was found with the mac but not with ip (already created
//								// element)
//								if (elementByMac != null && elementByIP == null) {
//		
//									// remove old ip (if exists) configured on the port with the same mac
//									if (portByMac.getIpAddress() != null) {
//										if (elementByMac.getIpAddressList() != null) {
//											elementByMac.getIpAddressList().remove(portByMac.getIpAddress());
//										}
//									}
//		
//									// set ip on port with related mac
//									if (elementByMac.getIpAddressList() == null) {
//										elementByMac.setIpAddressList(new HashSet<String>());
//									}
//									elementByMac.getIpAddressList().add(ip);
//									portByMac.setIpAddress(ip);
//		
//									// Update (but not discovery)
//									update(elementByMac);
//									update(portByMac);
//								} 
//							}
//						}
//					//}
//				}
//			}
//		});
//	}
	

	private Collection<Element> bootDiscovery() {
		return this.discover(serverLocalIpAddress, Boolean.TRUE, Boolean.FALSE);
	}
		
	private Collection<Element> discover(String ipAddress, Boolean discoverNeighborhood, Boolean justDiscoverNewDevices) {
		final Stack<String> ipsToDiscovery = new Stack<String>();
		ipsToDiscovery.add(ipAddress);
		return this.discover(ipsToDiscovery, discoverNeighborhood, justDiscoverNewDevices);
	}
	
	private Collection<Element> discover(final Stack<String> ipsToDiscovery, final Boolean discoverNeighborhood, final Boolean justDiscoverNewDevices) {
		final ExecutorService taskExecutor = Executors.newFixedThreadPool(16);
		final CountingLatch latch = new CountingLatch(0);
		final Map<String,Element> discoveredElements = new ConcurrentHashMap<String, Element>();
		final Map<String,Port> discoveredPorts = new ConcurrentHashMap<String, Port>();
		final Set<String> ipsDiscovered = Collections.synchronizedSet(new HashSet<String>());
		final Set<String> ipsFailed = Collections.synchronizedSet(new HashSet<String>());
		
		
		while (latch.getCount() > 0 || !ipsToDiscovery.isEmpty()) {
			if (!ipsToDiscovery.isEmpty()) {

				// get current IP of stack, countUp the task and add to set of already discovery
				final String currentIp = ipsToDiscovery.pop();
				ipsDiscovered.add(currentIp);
				latch.countUp();

				// run discovery task
				taskExecutor.submit(new Runnable() {
					@Override
					public void run() {
						try {
							// discover current ip
							Element element = elementService.discover(currentIp);
							if(element == null) {
								ipsFailed.add(currentIp);
								return;
							}
							
							// set ID
							element.setIdElement(UUID.randomUUID());
							for(Port port : element.getPortList()) {
								port.setIdElement(element.getIdElement());
								port.setIdPort(UUID.randomUUID());
								discoveredPorts.put(port.getMacAddress().trim(), port);
							}
							
							// merge and update element info
							setDiscoveryFields(element, new Date(), getInstanceDiscovery(), METHOD_BOOT, SOURCE_DISCOVERY);
							
							// set state and type
							setElementType(element);
							element.setState(ElementState.DISCOVERED);
							
							// set list discovered
							for(String ip : element.getIpAddressList()) {
								discoveredElements.put(ip,element);
								if(!ipsDiscovered.contains(ip)) {
									ipsDiscovered.add(ip);
								}
							}

							// verify neighbors
							for(Port port : element.getPortList()) {
								if(port.getRemoteIpAddress() != null) {
									String remoteIp = port.getRemoteIpAddress();
									if(!ipsToDiscovery.contains(remoteIp) && !ipsDiscovered.contains(remoteIp) && !remoteIp.startsWith("172.17")) {
										if(discoverNeighborhood) {
											if(justDiscoverNewDevices) {
												UUID idElement = ipToIdElementCache.get(remoteIp);
												if(idElement == null) {
													Element remoteElement = topologyService.getElementByIPAddress(remoteIp);
													indexIDs(remoteElement);
													if(remoteElement == null && !ipsToDiscovery.contains(remoteIp) && !ipsDiscovered.contains(remoteIp)) {
														ipsToDiscovery.add(remoteIp);
													}
												}
											}else {
												ipsToDiscovery.add(remoteIp);
											}
										}
									}
								}else {
									if(port.getRemoteMacAddress() != null) {
										if(macToIpCache.containsKey(port.getRemoteMacAddress())) {
											String remoteIp = macToIpCache.get(port.getRemoteMacAddress());
											if(!ipsToDiscovery.contains(remoteIp) && !ipsDiscovered.contains(remoteIp) && !remoteIp.startsWith("172.17")) {
												if(discoverNeighborhood) {
													if(justDiscoverNewDevices) {
														UUID idElement = ipToIdElementCache.get(remoteIp);
														if(idElement == null) {
															Element remoteElement = topologyService.getElementByIPAddress(remoteIp);
															indexIDs(remoteElement);
															if(remoteElement == null && !ipsToDiscovery.contains(remoteIp) && !ipsDiscovered.contains(remoteIp)) {
																ipsToDiscovery.add(remoteIp);
															}
														}
													}else {
														ipsToDiscovery.add(remoteIp);
													}
												}
											}
											port.setRemoteIpAddress(remoteIp);
										}
									}
								}
							}
																
						} finally {
							latch.countDown();
						}
					}
				});
			}

			try {
				if (ipsToDiscovery.isEmpty() && latch.getCount() > 0) {
					logger.info("Waiting... " + latch.getCount() + " discovery tasks running and " + ipsToDiscovery.size() + " devices to discovery.");
					Thread.sleep(100);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		taskExecutor.shutdown();
		
		for(Element element : discoveredElements.values()) {
			for(Port port : element.getPortList()) {
				if(port.getRemoteMacAddress() != null) {
					Port remotePort = discoveredPorts.get(port.getRemoteMacAddress());
					
					if(remotePort != null) {
						logger.info("Link found! "+element.getName()+"("+element.getIpAddressList().iterator().next()+")"+port.getPortName()+"("+port.getMacAddress()+") => ("+port.getRemoteIpAddress()+")::"+remotePort.getPortName()+"("+remotePort.getMacAddress()+")");	
						port.setRemoteIdPort(remotePort.getIdPort());
						remotePort.setRemoteIdPort(port.getIdPort());
					}else {
						logger.error("Remote MacAddress not found!"+element.getName()+"("+element.getIpAddressList().iterator().next()+")"+"::"+port.getPortName()+"("+port.getMacAddress()+") => "+port.getRemoteMacAddress());
					}
				}
			}
		}

		if(!ipsFailed.isEmpty()) {
			logger.info("Some discovery failed... "+ipsFailed);
			return new ArrayList<Element>();
		} else {
			return discoveredElements.values();
		}
	}

//	@Scheduled(fixedDelayString = "${topology.scoe.discovery.expiration.scheduler.interval:2000}")
//	public void updateElements() throws InterruptedException {
//		if(NETWORK_STATE_RUNNING.equals(network_state)) {
//			if(DISCOVERING_STATE_STOPPED.equals(discovery_state)) {
//				final Stack<String> ipsToDiscovery = new Stack<String>();
//				for (Element element : topologyService.getElements()) {
//					if (shouldDiscoveryImmediately(element) || (canRunDiscovery(element) && isLeafElement(element)) || ( canRunDiscovery(element) && hasDiscoveryAlreadyExpired(element))) {
//						logger.info("Element added to discovery "+element.getManagementIPAddressList()+" state:"+element.getState());
//						ipsToDiscovery.add(element.getManagementIPAddressList().iterator().next());
//					}
//				}
//	
//				if (!ipsToDiscovery.isEmpty()) {
//					discover(ipsToDiscovery, METHOD_EXPIRATION);
//				}
//			}else {
//				logger.info("Waiting last discovery routine...");
//			}
//		}else {
//			logger.info("Waiting network booting...");
//		}
//	}
//	
//	
//	
//	@Scheduled(fixedDelayString = "${topology.scoe.discovery.controller.scheduler.interval:1000}")
//	public void discoveryControlledDevices() {
//		if(NETWORK_STATE_RUNNING.equals(network_state)) {
//			logger.debug("Discovering network elements using 'Controller'...");
//			Collection<Element> elementList = elementService.discover();
//			if(elementList != null && !elementList.isEmpty()) {
//				for(Element element : elementList) {
//					// find persisted element
//					Element persistedElement = null;
//					for(String ipAddress : element.getManagementIPAddressList()) {
//						persistedElement = topologyService.getElementByIPAddress(ipAddress);
//						if(persistedElement != null) {
//							break;
//						}
//					}
//					
//					// if element is persisted
//					if(persistedElement != null) {
//						if(ElementState.WAITING_CONTROLLER_CONNECTION.equals(persistedElement.getState())) {
//							// update element
//							element.setState(ElementState.WAITING_CONFIGURATION);
//							
//							// save element
//							Element elementUpdated = mergeCascade(persistedElement, element, null, Boolean.FALSE);
//							
//							// send saved state event (initial state before finding the element in topology)
//							eventService.publish(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_WAITING_CONFIGURATION, elementUpdated);
//						}else {
//							if(persistedElement.getOfDeviceId() == null) {
//								// save element
//								mergeCascade(persistedElement, element, null, Boolean.FALSE);
//							}
//						}
//					}
//				}
//			}
//		}
//	}
//
//
//	/**
//	 * Main Logic
//	 */
//
//	private void discover(final String ip, final String method) {
//		final Stack<String> ipsToDiscovery = new Stack<String>();
//		ipsToDiscovery.add(ip);
//		this.discover(ipsToDiscovery, method);
//	}
//
//	private void discover(final Stack<String> ipsToDiscovery, final String method) {
//		discovery_state = DISCOVERING_STATE_STOPPED;
////		synchronized(this) {
//			try {
//				Set<Link> linksChanged =  Collections.synchronizedSet(new HashSet<Link>());
//				Map<Element, String> stateEventMap = new HashMap<Element, String>();
//				List<Element> discoveredElements = new ArrayList<Element>();
//				
//				runElementDiscoveryTasks(ipsToDiscovery, method, linksChanged, stateEventMap, discoveredElements);
//				
//				runNeighborhoodProcessing(discoveredElements, linksChanged, stateEventMap);
//				
//				//runCleanupAndRemoveOldNeighbors(linksChanged);
//						
//		
//				if (!linksChanged.isEmpty()) {
//					StringBuilder builder = new StringBuilder("-> " + linksChanged.size() + " links changed!");
//					for(Link link : linksChanged) {
//						builder.append("\n"+link.toString());
//					}
//					logger.info(builder.toString());
//					eventService.publish(SonarTopics.TOPIC_TOPOLOGY_LINKS_CHANGED, linksChanged);
//				}
//				
//				if(!stateEventMap.isEmpty()) {
//					for(Element element : stateEventMap.keySet()) {
//						eventService.publish(stateEventMap.get(element), element);
//					}
//				}
//		
//			}finally {
//				discovery_state = DISCOVERY_STATE_RUNNING;
//			}
////		}
//	}
//
//	private void runNeighborhoodProcessing(List<Element> discoveredElements, Set<Link> linksChanged, Map<Element, String> stateEventMap) {
//			//Work with neighbors
//		for(Element discoveredElement : discoveredElements) {
//			if(isOnFinalBootState(discoveredElement)) {
//				// add neighbors considering the method
//				Map<String,String> mapIpAndPortNeigbors = getNeighbors(discoveredElement);
//				for (String ipNeighbor : mapIpAndPortNeigbors.keySet()) {
//					if(!ipNeighbor.startsWith("172.17")) { //TODO Ignoring Docker IP's... review it
//						Element neighborPersisted = getElementByIP(ipNeighbor);
//						
//						Port port = getPortByRemoteIpOrRemoteMac(discoveredElement, ipNeighbor, mapIpAndPortNeigbors.get(ipNeighbor));
//						
//						if (neighborPersisted == null) {
//							// create basic representation of element
//							Element peerElement = buildBasicElementByIpAndMac(ipNeighbor, mapIpAndPortNeigbors.get(ipNeighbor), Boolean.FALSE);
//							peerElement.setState(ElementState.CONNECTED_TO_TOPOLOGY);
//							
//							// add port and set link on peerPort
//							Port peerPort = new Port();
//							peerPort.setMacAddress(mapIpAndPortNeigbors.get(ipNeighbor));
//							peerPort.setRemoteIdPort(port.getIdPort());
//							peerElement.setPortList(new HashSet<Port>(Arrays.asList(peerPort)));
//							
//							peerElement.setTopologyLevel(discoveredElement.getTopologyLevel()+1);
//							
//							// save element
//							Element peerElementUpdated = saveCascade(peerElement);
//							peerElement.setIdElement(peerElementUpdated.getIdElement());
//							
//							// set link on port
//							port.setRemoteIdPort(peerPort.getIdPort());
//							update(port);
//							
//							addLinkChange(linksChanged, port, peerPort, LinkEvent.CREATED);
//							
//							// send saved state event (initial state before finding the element in topology)
//							stateEventMap.put(peerElement, SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_CONNECTED_TO_TOPOLOGY);
//						}else {
//							
//							if(neighborPersisted.getPortList() == null || neighborPersisted.getPortList().isEmpty()) {
//								neighborPersisted.setPortList(getPortsByIdElement(neighborPersisted.getIdElement()));
//							}
//							boolean linkChanged = false;
//							
//							Port peerPort = getPortByMac(neighborPersisted, mapIpAndPortNeigbors.get(ipNeighbor));
//							if(peerPort != null) {
//								if(peerPort.getRemoteIdPort() == null || !peerPort.getRemoteIdPort().equals(port.getIdPort())) {
//									peerPort.setRemoteIdPort(port.getIdPort());
//									update(peerPort);
//									linkChanged = true;
//								}
//							}else {
//								peerPort = new Port();
//								peerPort.setMacAddress(mapIpAndPortNeigbors.get(ipNeighbor));
//								peerPort.setIdElement(neighborPersisted.getIdElement());
//								peerPort.setRemoteIdPort(port.getIdPort());
//								Port savedPeerPort = save(peerPort);
//								peerPort.setIdPort(savedPeerPort.getIdPort());
//								linkChanged = true;
//								neighborPersisted.getPortList().add(peerPort);
//							}
//							
//							// set link on port
//							if(port.getRemoteIdPort() == null || !port.getRemoteIdPort().equals(peerPort.getIdPort())) {
//								port.setRemoteIdPort(peerPort.getIdPort());
//								update(port);
//								linkChanged = true;
//							}
//							
//							String stateEventToFire = null;
//							if(neighborPersisted.getState().before(ElementState.CONNECTED_TO_TOPOLOGY)) {
//								neighborPersisted.setState(ElementState.CONNECTED_TO_TOPOLOGY);
//								stateEventToFire = SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_CONNECTED_TO_TOPOLOGY;
//							}else {
//								if(neighborPersisted.getState().equals(ElementState.WAITING_ROUTES)) {
//									stateEventToFire = SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_WAITING_ROUTES;	
//								}
//							}
//							
//							if(discoveredElement.getTopologyLevel() + 1 < neighborPersisted.getTopologyLevel()) {
//								neighborPersisted.setTopologyLevel(discoveredElement.getTopologyLevel()+1);
//							}
//							
//							// save element
//							Element elementUpdated = update(neighborPersisted);
//							neighborPersisted.setIdElement(elementUpdated.getIdElement());
//							
//							// if state changed: fire an event
//							if(stateEventToFire != null && !stateEventToFire.isEmpty()) {
//								stateEventMap.put(neighborPersisted, stateEventToFire);
//							}
//							// if link changed: add change
//							if(linkChanged) {
//								addLinkChange(linksChanged, port, peerPort, LinkEvent.CREATED);
//							}
//						}
//					}
//				}
//			}
//		}
//	}
//	
//	private boolean isOnFinalBootState(Element element) {
//		return  (ElementType.SERVER.equals(element.getTypeElement()) && ElementState.DISCOVERED.equals(element.getState())) ||
//				(ElementType.DEVICE.equals(element.getTypeElement()) && ElementState.CONFIGURED.equals(element.getState()));
//	}
//
//	private void runElementDiscoveryTasks(final Stack<String> ipsToDiscovery, final String method, final Set<Link> linksChanged, final Map<Element, String> stateEventMap, final List<Element> discoveredElements){
//		final ExecutorService taskExecutor = Executors.newFixedThreadPool(16);
//		final CountingLatch latch = new CountingLatch(0);
//		
//		final Set<String> ipsDiscovered = Collections.synchronizedSet(new HashSet<String>());
//		
//		while (latch.getCount() > 0 || !ipsToDiscovery.isEmpty()) {
//			if (!ipsToDiscovery.isEmpty()) {
//
//				// get current IP of stack, countUp the task and add to set of already discovery
//				final String currentIp = ipsToDiscovery.pop();
//				ipsDiscovered.add(currentIp);
//				latch.countUp();
//
//				// run discovery task
//				taskExecutor.execute(new Runnable() {
//					@Override
//					public void run() {
//						try {
//							// discover current ip
//							Element discoveredElement = elementService.discover(currentIp);
//
//							// find element already persisted
//							Element persistedElement = getElementByIP(currentIp);
//
//							// if discovery worked
//							if (discoveredElement != null) {
//								// and element is already persisted
//								if (persistedElement != null) {
//									String stateEventToFire = null;
//									if(!persistedElement.getState().equalsAny(ElementState.WAITING_CONTROLLER_CONNECTION, ElementState.CONFIGURED )) {
//										if(persistedElement.getOfDeviceId() != null && !persistedElement.getOfDeviceId().isEmpty()) {
//											persistedElement.setState(ElementState.WAITING_CONFIGURATION);
//											stateEventToFire = SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_WAITING_CONFIGURATION;
//										}else {
//											if(!persistedElement.getState().equals(ElementState.DISCOVERED)) {
//												persistedElement.setState(ElementState.DISCOVERED);
//												stateEventToFire = SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_DISCOVERED;
//											}else {
//												if( isDeviceWithOpenflowSupport(persistedElement) ) {
//													stateEventToFire = SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_WAITING_CONTROLLER_ASSIGNMENT;
//												}
//											}
//										}
//									}
//									
//									// merge and update element info
//									setDiscoveryFields(discoveredElement, new Date(), getInstanceDiscovery(), method, SOURCE_DISCOVERY);
//									Element updatedElement = mergeCascade(persistedElement, discoveredElement, linksChanged, Boolean.TRUE);
//									discoveredElement.setIdElement(updatedElement.getIdElement());
//									
//									// if state changed: fire an event
//									if(stateEventToFire != null && !stateEventToFire.isEmpty()) {
//										stateEventMap.put(discoveredElement, stateEventToFire);
//									}
//								} else { //if not...
//									// set discovery fields info
//									setDiscoveryFields(discoveredElement, new Date(), getInstanceDiscovery(), method, SOURCE_DISCOVERY);
//
//									// set Discovered state (bypass initial states)
//									discoveredElement.setState(ElementState.DISCOVERED);
//									discoveredElement.setTopologyLevel(0);
//									
//									// save the new element
//									Element updatedElement = saveCascade(discoveredElement);
//									discoveredElement.setIdElement(updatedElement.getIdElement());
//									
//									//fire and Discovered event
//									stateEventMap.put(discoveredElement, SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_DISCOVERED);
//								}
//								
//								discoveredElements.add(discoveredElement);
//							} else {
//								// if discovery not worked...
//								// but there is a persisted element
//								if (persistedElement != null) {
//									if (canRunDiscovery(persistedElement)) {
//										// if element was alredy designed as "DISCONNECTED" and elementMaxAttempts threshold was reached...
//										if(ElementState.DISCONNECTED.equals(persistedElement.getState()) ) {
//											if(maxAttemptsBeforeDelete <= persistedElement.getDiscoveryFailureCount()) {
//												//remove it!
//												removeCascade(persistedElement, linksChanged);
//											}else {
//												//else: increment count and update element without firing an event
//												persistedElement.incrementDiscoveryFailureCount();
//												setDiscoveryFields(persistedElement, new Date(), getInstanceDiscovery(), method, SOURCE_DISCOVERY);
//												topologyService.update(persistedElement);
//											}
//										}else {
//											if(persistedElement.getState().isInitialState() && !waitTimeOutReached(persistedElement)) {
////												if(!persistedElement.getState().equals(ElementState.IP_ASSIGNED_BY_DHCP)) {
//													persistedElement.setState(ElementState.WAITING_ROUTES);
//													update(persistedElement);
//													stateEventMap.put(persistedElement, SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_WAITING_ROUTES);
////												}
//											}else {
//												if(persistedElement.getState().equals(ElementState.WAITING_ROUTES) && !waitTimeOutReached(persistedElement)) {
//													//fire the event again
//													stateEventMap.put(persistedElement, SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_WAITING_ROUTES);
//												}else {
//													persistedElement.setState(ElementState.DISCONNECTED);
//													persistedElement.setDiscoveryFailureCount(1);
//													setDiscoveryFields(persistedElement, new Date(), getInstanceDiscovery(), method, SOURCE_DISCOVERY);
//													if(persistedElement.getPortList() == null || persistedElement.getPortList().isEmpty()) {
//														persistedElement.setPortList(getPortsByIdElement(persistedElement.getIdElement()));
//													}
//													for(Port port : persistedElement.getPortList()) {
//														boolean portChanged = false;
//														if (port.getRemoteIdPort() != null) {
//															addLinkChange(linksChanged, port, null, LinkEvent.REMOVED);
//															portChanged = true;
//														}
//														port.setRemoteHostname(null);
//														port.setRemoteIdPort(null);
//														port.setRemoteIpAddress(null);
//														port.setRemoteMacAddress(null);
//														port.setRemotePort(null);
//														port.setRemotePortId(null);
//														port.setRemotePortName(null);
//														
//														if(portChanged) {
//															update(port);
//														}
//													}
//													Element updatedElement = update(persistedElement);
//													stateEventMap.put(updatedElement, SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_DISCONNECTED);
//												}
//											}
//										}
//									}
//								} 
//							}
//						} finally {
//							latch.countDown();
//						}
//					}
//				});
//			}
//
//			try {
//				if (latch.getCount() > 0 || !ipsToDiscovery.isEmpty()) {
//					logger.debug("Waiting... " + latch.getCount() + " discovery tasks running and "
//							+ ipsToDiscovery.size() + " devices to discovery.");
//					Thread.sleep(1000);
//				}
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		taskExecutor.shutdown();
//	}
//
//	private Port getPortByMac(Element neighborPersisted, String mac) {
//		if(neighborPersisted.getPortList() != null) {
//			for(Port port : neighborPersisted.getPortList()) {
//				if(port.getMacAddress().equalsIgnoreCase(mac)) {
//					return port;
//				}
//			}
//		}
//		return null;
//	}
//
//	

//	public Port getPortByRemoteIpOrRemoteMac(Element element, String ip, String mac) {
//		if (element.getPortList() == null || element.getPortList().isEmpty()) {
//			element.setPortList(getPortsByIdElement(element.getIdElement()));
//		}
//
//		for (Port port : element.getPortList()) {
//			if (ip != null && port.getRemoteIpAddress() != null && port.getRemoteIpAddress().equals(ip)) {
//				return port;
//			} 
//			if (mac != null && port.getRemoteMacAddress() != null && port.getRemoteMacAddress().equals(mac)) {
//				return port;
//			}
//		}
//		return null;
//	}
	
	private Map<String, String> getNeighbors(Element element) {
		Map<String, String> neighbors = new HashMap<String, String>();
		if (element.getPortList() == null || element.getPortList().isEmpty()) {
			element.setPortList(getPortsByIdElement(element.getIdElement()));
		}
	
		for (Port port : element.getPortList()) {
			if (port.getRemoteIpAddress() != null) {
				neighbors.put(port.getRemoteIpAddress(), port.getRemoteMacAddress());
			} else {
				if (port.getRemoteMacAddress() != null) {
					Port remotePort = getPortByMac(port.getMacAddress());
					if (remotePort != null) {
						if (remotePort.getIpAddress() != null) {
							neighbors.put(remotePort.getIpAddress(), port.getRemoteMacAddress());
						} else {
							Element remoteElement = getElementById(remotePort.getIdElement());
							if (remoteElement != null) {
								neighbors.put(remoteElement.getIpAddressList().iterator().next(), port.getRemoteMacAddress());
							}
						}
					}
				}
			}
		}
		return neighbors;
	}

	
	/**
	 * CRUD
	 */
	private Element saveCascade(Element element) {
		synchronized (element.getIpAddressList().iterator().next()) {
			logger.info("New Element saved: " + ObjectUtils.toString(element));
			setElementType(element);
			Element savedElement = topologyService.save(element);
			element.setIdElement(savedElement.getIdElement());
			for (Port port : element.getPortList()) {
				port.setIdElement(savedElement.getIdElement());
				Port savedPort = topologyService.save(port);
				port.setIdPort(savedPort.getIdPort());
			}
			eventService.publish(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_ADDED, savedElement);
			return element;
		}
	}

	private void removeCascade(Element persistedElement, Set<Link> linksChanged) {
		synchronized (persistedElement.getIpAddressList().iterator().next()) {
			Set<Port> persistedPorts = getPortsByIdElement(persistedElement.getIdElement());
			if (persistedPorts != null && !persistedPorts.isEmpty()) {
				for (Port portRemoved : persistedPorts) {
					if (portRemoved.getRemoteIdPort() != null) {
						// Port linked being removed
						addLinkChange(linksChanged, portRemoved, null, LinkEvent.REMOVED );
					}
					remove(portRemoved);
				}
			}
			topologyService.deleteElementById(persistedElement.getIdElement());
			eventService.publish(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_REMOVED, persistedElement);
		}
	}

	private Element mergeCascade(Element persistedElement, Element discoveredElement, Set<Link> linksChanged, Boolean completeUpdateMerge) {
		synchronized (persistedElement.getIpAddressList().iterator().next()) {
			boolean elementDetailsChanged = false;
	
			// Verify all attributes
			if (discoveredElement.getClock() != null && (persistedElement.getClock() == null
					|| !discoveredElement.getClock().equals(persistedElement.getClock()))) {
				persistedElement.setClock(discoveredElement.getClock());
				elementDetailsChanged = true;
			} else {
				if(persistedElement.getClock() != null && discoveredElement.getClock() == null) {
					discoveredElement.setClock(persistedElement.getClock());
				}
			}
			if (discoveredElement.getCores() != null && (persistedElement.getCores() == null
					|| !discoveredElement.getCores().equals(persistedElement.getCores()))) {
				persistedElement.setCores(discoveredElement.getCores());
				elementDetailsChanged = true;
			} else {
				if(persistedElement.getCores() != null && discoveredElement.getCores() == null) {
					discoveredElement.setCores(persistedElement.getCores());
				}
			}
			if (discoveredElement.getCost() != null && (persistedElement.getCost() == null
					|| !discoveredElement.getCost().equals(persistedElement.getCost()))) {
				persistedElement.setCost(discoveredElement.getCost());
				elementDetailsChanged = true;
			} else {
				if(persistedElement.getCost() != null && discoveredElement.getCost() == null) {
					discoveredElement.setCost(persistedElement.getCost());
				}
			}
			if (discoveredElement.getDisk() != null && (persistedElement.getDisk() == null
					|| !discoveredElement.getDisk().equals(persistedElement.getDisk()))) {
				persistedElement.setDisk(discoveredElement.getDisk());
				elementDetailsChanged = true;
			} else {
				if(persistedElement.getDisk() != null && discoveredElement.getDisk() == null) {
					discoveredElement.setDisk(persistedElement.getDisk());
				}
			}
			if (discoveredElement.getEnergy() != null && (persistedElement.getEnergy() == null
					|| !discoveredElement.getEnergy().equals(persistedElement.getEnergy()))) {
				persistedElement.setEnergy(discoveredElement.getEnergy());
				elementDetailsChanged = true;
			} else {
				if(persistedElement.getEnergy() != null && discoveredElement.getEnergy() == null) {
					discoveredElement.setEnergy(persistedElement.getEnergy());
				}
			}
			if (discoveredElement.getMemory() != null && (persistedElement.getMemory() == null
					|| !discoveredElement.getMemory().equals(persistedElement.getMemory()))) {
				persistedElement.setMemory(discoveredElement.getMemory());
				elementDetailsChanged = true;
			} else {
				if(persistedElement.getMemory() != null && discoveredElement.getMemory() == null) {
					discoveredElement.setMemory(persistedElement.getMemory());
				}
			}
			if (discoveredElement.getName() != null && (persistedElement.getName() == null
					|| !discoveredElement.getName().equals(persistedElement.getName()))) {
				persistedElement.setName(discoveredElement.getName());
				elementDetailsChanged = true;
			} else {
				if(persistedElement.getName() != null && discoveredElement.getName() == null) {
					discoveredElement.setName(persistedElement.getName());
				}
			}
			if (discoveredElement.getTypeElement() != null && (persistedElement.getTypeElement() == null
					|| !discoveredElement.getTypeElement().equals(persistedElement.getTypeElement()))) {
				persistedElement.setTypeElement(discoveredElement.getTypeElement());
				elementDetailsChanged = true;
			} else {
				if(persistedElement.getTypeElement() != null && discoveredElement.getTypeElement() == null) {
					discoveredElement.setTypeElement(persistedElement.getTypeElement());
				}
			}
			if (discoveredElement.getState() != null && (persistedElement.getState() == null
					|| !discoveredElement.getState().equals(persistedElement.getState()))) {
				persistedElement.setState(discoveredElement.getState());
				elementDetailsChanged = true;
			} else {
				if(persistedElement.getState() != null && discoveredElement.getState() == null) {
					discoveredElement.setState(persistedElement.getState());
				}
			}
			if (discoveredElement.getOfChannel() != null && (persistedElement.getOfChannel() == null
					|| !discoveredElement.getOfChannel().equals(persistedElement.getOfChannel()))) {
				persistedElement.setOfChannel(discoveredElement.getOfChannel());
				elementDetailsChanged = true;
			} else {
				if(persistedElement.getOfChannel() != null && discoveredElement.getOfChannel() == null) {
					discoveredElement.setOfChannel(persistedElement.getOfChannel());
				}
			}
			if (discoveredElement.getIpAddressList() != null
					&& (persistedElement.getIpAddressList() == null || !persistedElement
							.getIpAddressList().containsAll(discoveredElement.getIpAddressList()))) {
				persistedElement.getIpAddressList().addAll(discoveredElement.getIpAddressList());
				elementDetailsChanged = true;
			} else {
				if(persistedElement.getIpAddressList() != null) {
					discoveredElement.getIpAddressList().addAll(persistedElement.getIpAddressList());
				}
			}
			if (discoveredElement.getOfControllerList() != null && (persistedElement.getOfControllerList() == null
					|| !persistedElement.getOfControllerList().containsAll(discoveredElement.getOfControllerList()))) {
				persistedElement.getOfControllerList().addAll(discoveredElement.getOfControllerList());
				elementDetailsChanged = true;
			} else {
				if(persistedElement.getOfControllerList() != null && discoveredElement.getOfControllerList() == null) {
					discoveredElement.setOfControllerList(persistedElement.getOfControllerList());
				}
			}
			if (discoveredElement.getOfDeviceId() != null && (persistedElement.getOfDeviceId() == null
					|| !discoveredElement.getOfDeviceId().equals(persistedElement.getOfDeviceId()))) {
				persistedElement.setOfDeviceId(discoveredElement.getOfDeviceId());
				elementDetailsChanged = true;
			} else {
				if(persistedElement.getOfDeviceId() != null && discoveredElement.getOfDeviceId() == null) {
					discoveredElement.setOfDeviceId(persistedElement.getOfDeviceId());
				}
			}
			if (discoveredElement.getManufacturer() != null && (persistedElement.getManufacturer() == null
					|| !discoveredElement.getManufacturer().equals(persistedElement.getManufacturer()))) {
				persistedElement.setManufacturer(discoveredElement.getManufacturer());
				elementDetailsChanged = true;
			} else {
				if(persistedElement.getManufacturer() != null && discoveredElement.getManufacturer() == null) {
					discoveredElement.setManufacturer(persistedElement.getManufacturer());
				}
			}
			if (discoveredElement.getProduct() != null && (persistedElement.getProduct() == null
					|| !discoveredElement.getProduct().equals(persistedElement.getProduct()))) {
				persistedElement.setProduct(discoveredElement.getProduct());
				elementDetailsChanged = true;
			} else {
				if(persistedElement.getProduct() != null && discoveredElement.getProduct() == null) {
					discoveredElement.setProduct(persistedElement.getProduct());
				}
			}
			if (discoveredElement.getSoftware() != null && (persistedElement.getSoftware() == null
					|| !discoveredElement.getSoftware().equals(persistedElement.getSoftware()))) {
				persistedElement.setSoftware(discoveredElement.getSoftware());
				elementDetailsChanged = true;
			} else {
				if(persistedElement.getSoftware() != null && discoveredElement.getSoftware() == null) {
					discoveredElement.setSoftware(persistedElement.getSoftware());
				}
			}
			
			if(discoveredElement.getLastDicoveredAt() != null) {
				persistedElement.setLastDicoveredAt(discoveredElement.getLastDicoveredAt());
			}else {
				if(persistedElement.getLastDicoveredAt() != null) {
					discoveredElement.setLastDicoveredAt(persistedElement.getLastDicoveredAt());
				}
			}
			if(discoveredElement.getLastDicoveredBy() != null) {
				persistedElement.setLastDicoveredBy(discoveredElement.getLastDicoveredBy());
			}else {
				if(persistedElement.getLastDicoveredBy() != null) {
					discoveredElement.setLastDicoveredBy(persistedElement.getLastDicoveredBy());
				}
			}
			if(discoveredElement.getLastDicoveredMethod() != null) {
				persistedElement.setLastDicoveredMethod(discoveredElement.getLastDicoveredMethod());
			}else {
				if(persistedElement.getLastDicoveredMethod() != null) {
					discoveredElement.setLastDicoveredMethod(persistedElement.getLastDicoveredMethod());
				}
			}
			if(discoveredElement.getLastDicoveredSource() != null) {
				persistedElement.setLastDicoveredSource(discoveredElement.getLastDicoveredSource());
			}else {
				if(persistedElement.getLastDicoveredSource() != null) {
					discoveredElement.setLastDicoveredSource(persistedElement.getLastDicoveredSource());
				}
			}
			
			Set<Port> persistedPorts = getPortsByIdElement(persistedElement.getIdElement());
			if (persistedPorts == null) {
				persistedPorts = new HashSet<Port>();
			}
	
			Map<String, Port> mapPersistedPort = new HashMap<String, Port>();
			Set<Port> portsRemoved = new HashSet<Port>();
			for (Port persistedPort : persistedPorts) {
				mapPersistedPort.put(persistedPort.getMacAddress(), persistedPort);
				portsRemoved.add(persistedPort);
			}
	
			for (Port discoveredPort : discoveredElement.getPortList()) {
				// update port
				if (mapPersistedPort.containsKey(discoveredPort.getMacAddress())) {
					Port persistedPort = mapPersistedPort.get(discoveredPort.getMacAddress());
	
					portsRemoved.remove(persistedPort);
	
					boolean portChanged = false;
					if (discoveredPort.getSpeed() != null && (persistedPort.getSpeed() == null
							&& !discoveredPort.getSpeed().equals(persistedPort.getSpeed()))) {
						persistedPort.setSpeed(discoveredPort.getSpeed());
						portChanged = true;
					} else {
						if(persistedPort.getSpeed() != null && discoveredPort.getSpeed() == null) {
							discoveredPort.setSpeed(persistedPort.getSpeed());
						}
					}
					if (discoveredPort.getPortId() != null && (persistedPort.getPortId() == null
							&& !discoveredPort.getPortId().equals(persistedPort.getPortId()))) {
						persistedPort.setPortId(discoveredPort.getPortId());
						portChanged = true;
					} else {
						if(persistedPort.getPortId() != null && discoveredPort.getPortId() == null) {
							discoveredPort.setPortId(persistedPort.getPortId());
						}
					}
					if (discoveredPort.getPortName() != null && (persistedPort.getPortName() == null
							&& !discoveredPort.getPortName().equals(persistedPort.getPortName()))) {
						persistedPort.setPortName(discoveredPort.getPortName());
						portChanged = true;
					} else {
						if(persistedPort.getPortName() != null && discoveredPort.getPortName() == null) {
							discoveredPort.setPortName(persistedPort.getPortName());
						}
					}
					if (discoveredPort.getIpAddress() != null && (persistedPort.getIpAddress() == null
							&& !discoveredPort.getIpAddress().equals(persistedPort.getIpAddress()))) {
						persistedPort.setIpAddress(discoveredPort.getIpAddress());
						portChanged = true;
					} else {
						if(persistedPort.getIpAddress() != null && discoveredPort.getIpAddress() == null) {
							discoveredPort.setIpAddress(persistedPort.getIpAddress());
						}
					}
					if (discoveredPort.getMacAddress() != null && (persistedPort.getMacAddress() == null
							&& !discoveredPort.getMacAddress().equals(persistedPort.getMacAddress()))) {
						persistedPort.setMacAddress(discoveredPort.getMacAddress());
						portChanged = true;
					} else {
						if(persistedPort.getMacAddress() != null && discoveredPort.getMacAddress() == null) {
							discoveredPort.setMacAddress(persistedPort.getMacAddress());
						}
					}
					if (discoveredPort.getOfPort() != null && (persistedPort.getOfPort() == null
							&& !discoveredPort.getOfPort().equals(persistedPort.getOfPort()))) {
						persistedPort.setOfPort(discoveredPort.getOfPort());
						portChanged = true;
					} else {
						if(persistedPort.getOfPort() != null && discoveredPort.getOfPort() == null) {
							discoveredPort.setOfPort(persistedPort.getOfPort());
						}
					}
					if (discoveredPort.getState() != null && (persistedPort.getState() == null
							&& !discoveredPort.getState().equals(persistedPort.getState()))) {
						persistedPort.setState(discoveredPort.getState());
						portChanged = true;
					} else {
						if(persistedPort.getState() != null && discoveredPort.getState() == null) {
							discoveredPort.setState(persistedPort.getState());
						}
					}
					if (discoveredPort.getAdminState() != null && (persistedPort.getAdminState() == null
							&& !discoveredPort.getAdminState().equals(persistedPort.getAdminState()))) {
						persistedPort.setAdminState(discoveredPort.getAdminState());
						portChanged = true;
					} else {
						if(persistedPort.getAdminState() != null && discoveredPort.getAdminState() == null) {
							discoveredPort.setAdminState(persistedPort.getAdminState());
						}
					}
	
					if (completeUpdateMerge) {
						if (discoveredPort.getRemotePortId() == null
								|| (discoveredPort.getRemotePortId() != null && (persistedPort.getRemotePortId() == null
										&& !discoveredPort.getRemotePortId().equals(persistedPort.getRemotePortId())))) {
							persistedPort.setRemotePortId(discoveredPort.getRemotePortId());
							portChanged = true;
						}
						if (discoveredPort.getRemoteIpAddress() == null || (discoveredPort.getRemoteIpAddress() != null
								&& (persistedPort.getRemoteIpAddress() == null && !discoveredPort.getRemoteIpAddress()
										.equals(persistedPort.getRemoteIpAddress())))) {
							persistedPort.setRemoteIpAddress(discoveredPort.getRemoteIpAddress());
							portChanged = true;
						}
						if (discoveredPort.getRemoteMacAddress() == null || (discoveredPort.getRemoteMacAddress() != null
								&& (persistedPort.getRemoteMacAddress() == null && !discoveredPort.getRemoteMacAddress()
										.equals(persistedPort.getRemoteMacAddress())))) {
							persistedPort.setRemoteMacAddress(discoveredPort.getRemoteMacAddress());
							portChanged = true;
						}
						if (discoveredPort.getRemoteHostname() == null || (discoveredPort.getRemoteHostname() != null
								&& (persistedPort.getRemoteHostname() == null && !discoveredPort.getRemoteHostname()
										.equals(persistedPort.getRemoteHostname())))) {
							persistedPort.setRemoteHostname(discoveredPort.getRemoteHostname());
							portChanged = true;
						}
						if (discoveredPort.getRemotePortName() == null || (discoveredPort.getRemotePortName() != null
								&& (persistedPort.getRemotePortName() == null && !discoveredPort.getRemotePortName()
										.equals(persistedPort.getRemotePortName())))) {
							persistedPort.setRemotePortName(discoveredPort.getRemotePortName());
							portChanged = true;
						}
					}
	
					if (portChanged) {
						update(persistedPort);
					}
					discoveredPort.setIdPort(persistedPort.getIdPort());
					discoveredPort.setIdElement(persistedPort.getIdElement());
					discoveredPort.setRemoteIdPort(persistedPort.getRemoteIdPort());
				} else {
					discoveredPort.setIdElement(persistedElement.getIdElement());
					Port savedPort = save(discoveredPort);
					discoveredPort.setIdPort(savedPort.getIdPort());
					discoveredPort.setRemoteIdPort(savedPort.getRemoteIdPort());
				}
			}
	
			if(completeUpdateMerge) {
				for (Port portRemoved : portsRemoved) {
					if (portRemoved.getRemoteIdPort() != null) {
						// Port linked being removed
						addLinkChange(linksChanged, portRemoved, null, LinkEvent.REMOVED );
					}
					remove(portRemoved);
				}
			}
			
			discoveredElement.setIdElement(persistedElement.getIdElement());
			if(discoveredElement.getTypeElement() == null) {
				setElementType(persistedElement);
				discoveredElement.setTypeElement(persistedElement.getTypeElement());
			}
			
			if (elementDetailsChanged) {
				return update(persistedElement);
			} else {
				// update to set timeout, but without events
				return topologyService.update(persistedElement);
			}
		}
	}

	private void addLinkChange(Set<Link> linksChanged, Port portAImage, Port portBImage, LinkEvent event) {
		Port portA = new Port();
		Port portB = null;
		
		portA = ObjectUtils.clone(portAImage);
		if(portBImage != null) {
			portB = ObjectUtils.clone(portBImage);
		}else {
			if(portA.getRemoteIdPort() != null) {
				portB = new Port();
				portB.setIdPort(portA.getRemoteIdPort());
				portB.setRemoteIdPort(portA.getIdPort());
				portB.setMacAddress(portA.getRemoteMacAddress());
				portB.setPortName(portA.getRemotePortName());
				portB.setPortId(portA.getRemotePortId());
			}
		}
		
		for(Link link : linksChanged) {
			if(link.getPortA() != null) {
				if(link.getPortA().getIdPort().equals(portA.getIdPort())) {
					if(link.getPortB() == null) {
						link.setPortB(portB);
					}else {
						if(link.getPortB().getIdPort().equals(portB.getIdPort())) {
							if(portB.getMacAddress() != null) {
								link.getPortB().setMacAddress(portB.getMacAddress());
							}
						}else {
							link.setPortB(portB);
						}
					}
					
					if(portA.getMacAddress() != null) {
						link.getPortA().setMacAddress(portA.getMacAddress());
					}
					return;
				}else {
					if(link.getPortA().getIdPort().equals(portB.getIdPort())) {
						
						if(link.getPortB() == null) {
							link.setPortB(portA);
						}else {
							if(link.getPortB().getIdPort().equals(portA.getIdPort())) {
								if(portA.getMacAddress() != null) {
									link.getPortB().setMacAddress(portA.getMacAddress());
								}
							}else {
								link.setPortB(portA);
							}
						}
						
						if(portB.getMacAddress() != null) {
							link.getPortA().setMacAddress(portB.getMacAddress());
						}
						return;
					}
				}
			}else {
				if(link.getPortB().getIdPort().equals(portA.getIdPort())) {
					link.setPortA(portB);
					
					if(portA.getMacAddress() != null) {
						link.getPortB().setMacAddress(portA.getMacAddress());
					}
					return;
				}else {
					if(link.getPortB().getIdPort().equals(portB.getIdPort())) {
						link.setPortA(portA);
						if(portB.getMacAddress() != null) {
							link.getPortB().setMacAddress(portB.getMacAddress());
						}
						return;
					}
				}
			}
		}
		
		linksChanged.add(new Link(portA, portB, event));
	}

	private Port update(Port port) {
		synchronized (port.getMacAddress()) {
			Port portSaved = topologyService.update(port);
			eventService.publish(SonarTopics.TOPIC_TOPOLOGY_PORT_CHANGED, portSaved);
			return portSaved;
		}
	}

	private Element update(Element element) {
		synchronized (element.getIpAddressList().iterator().next()) {
			Element elementSaved = topologyService.update(element);
			eventService.publish(SonarTopics.TOPIC_TOPOLOGY_ELEMENT_CHANGED_DETAILS, elementSaved);
			return elementSaved;
		}
	}

	private Port save(Port port) {
		synchronized (port.getMacAddress()) {
			Port portSaved = topologyService.save(port);
			eventService.publish(SonarTopics.TOPIC_TOPOLOGY_PORT_ADDED, portSaved);
			return portSaved;
		}
	}

	private void remove(Port port) {
		synchronized (port.getMacAddress()) {
			topologyService.delete(port);
			eventService.publish(SonarTopics.TOPIC_TOPOLOGY_PORT_REMOVED, port);
		}
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
	 * Util
	 */
	private String getInstanceDiscovery() {
		return "tscoe-" + serverLocalIpAddress;
	}

	private Boolean hasDiscoveryAlreadyExpired(Element element) {
		Date lastUpdate = element.getLastDicoveredAt();
		
		if(lastUpdate == null) {
			return Boolean.TRUE;
		}
		
		Calendar whenUpdate = new GregorianCalendar();
		whenUpdate.setTime(lastUpdate);
		whenUpdate.add(Calendar.MILLISECOND, discoveryExpirationTimeout);

		Calendar now = new GregorianCalendar();
		now.setTime(new Date());

		return !now.before(whenUpdate);
	}
	
	private Boolean waitTimeOutReached(Element element) {
		Date lastUpdate = element.getLastDicoveredAt();
		
		if(lastUpdate == null) {
			return Boolean.TRUE;
		}
		
		Calendar whenUpdate = new GregorianCalendar();
		whenUpdate.setTime(lastUpdate);
		whenUpdate.add(Calendar.MILLISECOND, waitingStateTimeout * 100);

		Calendar now = new GregorianCalendar();
		now.setTime(new Date());

		return !now.before(whenUpdate);
	}
	
	private Boolean canRunDiscovery(Element element) {
		return !element.getState().equalsAny(ElementState.WAITING_CONFIGURATION, ElementState.WAITING_CONTROLLER_CONNECTION) || waitTimeOutReached(element);
	}
	
	private boolean shouldDiscoveryImmediately(Element element) {
		return ElementState.WAITING_DISCOVERY.equals(element.getState());
	}
	
	private boolean isLeafElement(Element element) {
		return getNeighbors(element).size() <= 1;
	}
	
	private boolean isDeviceWithOpenflowSupport(Element persistedElement) {
		return persistedElement.getTypeElement().equals(ElementType.DEVICE);
	}

	private void setElementType(Element element) {
		// Temporary solution: TODO fix this setElementType
		if (element.getName() != null && element.getName().startsWith("sw")) {
			element.setTypeElement(ElementType.DEVICE);
		} else {
			if (element.getName() != null && (element.getName().startsWith("sonar-server")
					|| element.getName().startsWith("nfvi") || element.getName().startsWith("nfci"))) {
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
	
	private Element buildBasicElementByIpAndMac(String ip, String mac, Boolean assingIpToPort) {
		Element element = new Element();
		element.setIpAddressList(new HashSet<String>(Arrays.asList(ip)));
		if(mac != null) {
			Port port = new Port();
			port.setMacAddress(mac);
			if(assingIpToPort) {
				port.setIpAddress(ip);
			}
			element.setPortList(new HashSet<Port>(Arrays.asList(port)));
		}
		return element;
	}
}
