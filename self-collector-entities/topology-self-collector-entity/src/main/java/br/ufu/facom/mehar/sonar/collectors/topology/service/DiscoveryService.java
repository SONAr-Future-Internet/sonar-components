package br.ufu.facom.mehar.sonar.collectors.topology.service;

import java.net.InterfaceAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;
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
import br.ufu.facom.mehar.sonar.core.model.topology.Element;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;
import br.ufu.facom.mehar.sonar.core.util.IPUtils;
import br.ufu.facom.mehar.sonar.core.util.ObjectUtils;

@Component
public class DiscoveryService {
	
	private Logger logger = Logger.getLogger(DiscoveryService.class);
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private TopologyService topologyService;
	
	@Value("${topology.scoe.discovery.element.updateInterval:600000}")
	private Integer elementUpdateInterval;
	
	@Value("${topology.scoe.discovery.strategy.flooding.first:192.168.0.1}")
	private String floodingIntervalFirst;
	
	@Value("${topology.scoe.discovery.strategy.flooding.last:192.168.0.254}")
	private String floodingIntervalLast;
	
	
	
	
	@EventListener(ApplicationReadyEvent.class)
	public void listenToEvents() throws InterruptedException {
		eventService.subscribe(SonarTopics.TOPIC_TOPOLOGY_PORT_IP_ASSIGNED, new NetworkEventAction() {
			@Override
			public void handle(String event, String json) {
				Port port = ObjectUtils.toObject(json, Port.class);
				String ip = port.getIpAddress();
				String mac = port.getMacAddress();
				
				Element element = topologyService.getElementByIPAddress(ip);
				if(element == null || shouldUpdate(element)) {
					
				}
			}
		});
		
		
		System.out.println("Running A...");
		Thread.sleep(10000);
	}
	
	private Boolean shouldUpdate(Element element) {
		Date lastUpdate = element.getLastDicoveredAt();
		
		Calendar whenUpdate = new GregorianCalendar();
		whenUpdate.setTime(lastUpdate);
		whenUpdate.add(Calendar.SECOND, elementUpdateInterval);
		
		Calendar now = new GregorianCalendar();
		now.setTime(new Date());
		
		
		return now.before(whenUpdate);
	}
	
	@Scheduled(fixedDelayString = "${topology.scoe.discovery.strategy.bfs.interval:600000}")
	public void discoveryBFS() throws InterruptedException {
		InterfaceAddress interfaceAddress = IPUtils.searchActiveInterfaceAddress();
		if(interfaceAddress != null && interfaceAddress.getAddress() != null) {
			String rootIp = interfaceAddress.getAddress().toString();
			Element element = topologyService.getElementByIPAddress(rootIp);
			if(element == null) {
				element = new Element();
				element.setManagementIPAddressList(Sets.newHashSet(rootIp));
			}
			discoverOrUpdateElement(element);
		}
	}
	
	@Scheduled(fixedDelayString = "${topology.scoe.discovery.strategy.flooding.interval:60000000}")
	public void discoveryFlooding() throws InterruptedException {
		String currentIp = floodingIntervalFirst;
		while(!currentIp.equals(floodingIntervalLast)) {
			Element element = topologyService.getElementByIPAddress(currentIp);
			if(element == null) {
				element = new Element();
				element.setManagementIPAddressList(Sets.newHashSet(currentIp));
				discoverOrUpdateElement(element);
			}
			currentIp = IPUtils.nextIP(currentIp);
		}
	}
	
	@Scheduled(fixedDelayString = "${topology.scoe.discovery.element.updateInterval:600000}")
	public void updateElements() throws InterruptedException {
		for(Element element : topologyService.getElements()) {
			if(shouldUpdate(element) && !isOnDiscoveryQueue(element)) {
				discoverOrUpdateElement(element);
			}
		}
	}

	private void discoverOrUpdateElement(Element element) {
		// TODO Auto-generated method stub
		
	}

	private boolean isOnDiscoveryQueue(Element element) {
		// TODO Auto-generated method stub
		return false;
	}
}
