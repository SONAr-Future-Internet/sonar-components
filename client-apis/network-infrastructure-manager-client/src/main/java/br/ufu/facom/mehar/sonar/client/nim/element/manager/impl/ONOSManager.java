package br.ufu.facom.mehar.sonar.client.nim.element.manager.impl;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.net.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import br.ufu.facom.mehar.sonar.client.nim.component.manager.SonarCIMContainerManager;
import br.ufu.facom.mehar.sonar.client.nim.element.manager.SDNManager;
import br.ufu.facom.mehar.sonar.client.nim.element.model.ElementModelTranslator;
import br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow.ONOSDevice;
import br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow.ONOSDiscovery;
import br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow.ONOSFlowGroup;
import br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow.ONOSFlowRecord;
import br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow.ONOSFlowResponse;
import br.ufu.facom.mehar.sonar.client.nim.exception.DeviceConfigurationException;
import br.ufu.facom.mehar.sonar.client.nim.exception.DeviceConfigurationTimeoutException;
import br.ufu.facom.mehar.sonar.core.model.configuration.Flow;
import br.ufu.facom.mehar.sonar.core.model.core.Controller;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;
import br.ufu.facom.mehar.sonar.core.util.ObjectUtils;

@Component("onos")
public class ONOSManager implements SDNManager {
	Logger logger = LoggerFactory.getLogger(SonarCIMContainerManager.class);
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Override
	public Collection<Element> discover(Controller controller) {
		logger.debug(this.getClass().getCanonicalName() + "Get all devices on manager ONOS ["+controller.getNorth()+"]");
		Map<String, Element> responseMap = new HashMap<String, Element>();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
			headers.set("Authorization", "Basic " + Base64.encodeBase64String((controller.getAuthUsername()+":"+controller.getAuthPassword()).getBytes(StandardCharsets.UTF_8)).trim() );
			
			ResponseEntity<ONOSDiscovery> responseDeviceList = restTemplate.exchange(
					"http://"+controller.getNorth()+"/onos/v1/devices",
					HttpMethod.GET, new HttpEntity<>(headers),
					new ParameterizedTypeReference<ONOSDiscovery>() {
					});
	
			ONOSDiscovery discovery = responseDeviceList.getBody();
			if(discovery != null && discovery.getDevices() != null && !discovery.getDevices().isEmpty()) {
				for(ONOSDevice onosDevice : discovery.getDevices()) {
					if(!responseMap.containsKey(onosDevice.getId())) {
						//Get all device information and ports
						ResponseEntity<ONOSDevice> responseDevice = restTemplate.exchange(
								"http://"+controller.getNorth()+"/onos/v1/devices/"+onosDevice.getId()+"/ports",
								HttpMethod.GET, new HttpEntity<>(headers),
								new ParameterizedTypeReference<ONOSDevice>() {
								});
						
						ONOSDevice completeOnosDevice = responseDevice.getBody();
						if(completeOnosDevice.getAvailable()) {
							responseMap.put(completeOnosDevice.getId(), ElementModelTranslator.convertToElement(completeOnosDevice));
						}
					}
				}
			}
		} catch(Exception e) {
			logger.error("Error discovering devices on ONOS Controller with endpoint:"+controller.getNorth(), e);
		}
		return responseMap.values();
	}
	
	@Override
	public Set<String> configureFlows(Controller controller, Element element, List<Flow> flows) {
		for(Flow flow : flows) {
			if(flow.getDeviceId() == null) {
				flow.setDeviceId(element.getOfDeviceId());
			}
		}
		return this.configureFlows(controller, flows, Boolean.TRUE);
	}
	
	@Override
	public Set<String> configureFlows(Controller controller, List<Flow> flows, Boolean waitFlowCreation) {
		ONOSFlowGroup group = ElementModelTranslator.convertToONOSFlow(flows);
		
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
			headers.set("Authorization", "Basic " + Base64.encodeBase64String((controller.getAuthUsername()+":"+controller.getAuthPassword()).getBytes(StandardCharsets.UTF_8)).trim() );
			
			logger.info("Creating flows! => "+ObjectUtils.fromObject(group));
			
			ResponseEntity<ONOSFlowResponse> responseDeviceList = restTemplate.exchange(
					"http://"+controller.getNorth()+"/onos/v1/flows",
					HttpMethod.POST, new HttpEntity<>(group, headers),
					new ParameterizedTypeReference<ONOSFlowResponse>() {
					});
	
			ONOSFlowResponse result = responseDeviceList.getBody();
			
			//Wait while flows are pending
			Set<String> flowSet = new HashSet<String>();
			for(ONOSFlowRecord flow : result.getFlows()) {
				flowSet.add(flow.getFlowId());
			}
			
			if(waitFlowCreation) {
				waitFlowCreation(controller, flowSet);
			}

			return flowSet;
			
		} catch(Exception e) {
			if(! (e instanceof DeviceConfigurationTimeoutException)) {
				throw new DeviceConfigurationException("Error pushing flows on ONOS Controller with endpoint:"+controller.getNorth(), e);
			}
			throw e;
		}
	}

	private void waitFlowCreation(Controller controller, Set<String> flowSet) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		headers.set("Authorization", "Basic " + Base64.encodeBase64String((controller.getAuthUsername()+":"+controller.getAuthPassword()).getBytes(StandardCharsets.UTF_8)).trim() );
		
		Date startDate = new Date();
		Set<String> flowPendingSet = new HashSet<String>(flowSet); 
		while(!flowPendingSet.isEmpty()){
			flowPendingSet.clear();
			ResponseEntity<ONOSFlowResponse> responsePending = restTemplate.exchange(
					"http://"+controller.getNorth()+"/onos/v1/flows",
					HttpMethod.GET, new HttpEntity<>(headers),
					new ParameterizedTypeReference<ONOSFlowResponse>() {
					});

			ONOSFlowResponse deviceFlows = responsePending.getBody();
			
			
			for(ONOSFlowRecord flow : deviceFlows.getFlows()) {
				if(flowSet.contains(flow.getId()) && !flow.getState().equalsIgnoreCase("ADDED")) {
					flowPendingSet.add(flow.getId());
				}
			}
			
			if(timeoutReached(startDate, 10*flowSet.size()) && !flowPendingSet.isEmpty()) {
				throw new DeviceConfigurationTimeoutException("Configuration timeout for pending flows "+flowPendingSet);
			}else {
				logger.debug("Current flows:"+ObjectUtils.fromObject(responsePending));
			}
			
			if(!flowPendingSet.isEmpty()) {
				try {
					logger.info("Waiting for pending flows... "+flowPendingSet);
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private boolean timeoutReached(Date startDate, int timeInSecs) {
		Calendar whenTimeout = new GregorianCalendar();
		whenTimeout.setTime(startDate);
		whenTimeout.add(Calendar.SECOND, timeInSecs);

		Calendar now = new GregorianCalendar();
		now.setTime(new Date());

		return !now.before(whenTimeout);
	}


	
	public static void main(String[] args) {
		ONOSManager onosManager = new ONOSManager();
		onosManager.restTemplate = new RestTemplate();
		Controller controller = new Controller();
		controller.setNorth("192.168.0.1:8181");
		controller.setAuthUsername("onos");
		controller.setAuthPassword("rocks");
	
		for(Element element : onosManager.discover(controller)) {
			System.out.println(element.getIpAddressList() + " : "+element.getOfDeviceId());
			for(Port port : element.getPortList()) {
				System.out.println("\t"+port.getMacAddress()+" : "+port.getOfPort());
			}
		}
	}
	
	/*public static void main(String[] args) {
		ONOSManager onosManager = new ONOSManager();
		onosManager.restTemplate = new RestTemplate();
		onosManager.configuration = new NIMConfiguration();
		onosManager.configuration.setSDNNorthSeedsAttr("192.168.0.1:8181");
		onosManager.configuration.setSDNNorthAuthStringAttr("onos:rocks");
		
		List<Flow> flows =  new ArrayList<Flow>();
		//Generic ARP Flow
		flows.add(new Flow(new FlowSelector(FlowSelectorType.ETH_TYPE, "0x0806"), new FlowInstruction(FlowInstructionType.NORMAL)));//
		
		//Generic LLDP Flow
		flows.add(new Flow(new FlowSelector(FlowSelectorType.ETH_TYPE, "0x88CC"), new FlowInstruction(FlowInstructionType.NORMAL)));
		
		//Device specifics
		{
			Element element = new Element();
			element.setOfDeviceId("of:00001ea738cb2c4d");
			element.setManagementIPAddressList(new HashSet<String>(Arrays.asList("192.168.0.2")));
			List<Flow> elementFlows = new ArrayList<Flow>(flows);
			
			//Route to Server
			elementFlows.add(new Flow(
				Arrays.asList(
					new FlowSelector(FlowSelectorType.ETH_TYPE, "0x0800"), 
					new FlowSelector(FlowSelectorType.IPV4_DST, "192.168.0.1/32")
				), 
				Arrays.asList(
					new FlowInstruction(FlowInstructionType.OUTPUT, "1")
				)
			));
			
			
			//Local Processing
			
			for(String address : element.getManagementIPAddressList()) {
				elementFlows.add(new Flow(
						Arrays.asList(
							new FlowSelector(FlowSelectorType.ETH_TYPE, "0x0800"), 
							new FlowSelector(FlowSelectorType.IPV4_DST, address+"/32")
						), 
						Arrays.asList(
							new FlowInstruction(FlowInstructionType.NORMAL)
						)
					));
			}
			
			
			//Neighbors Route
			elementFlows.add(new Flow(
					Arrays.asList(
						new FlowSelector(FlowSelectorType.ETH_TYPE, "0x0800"), 
						new FlowSelector(FlowSelectorType.IPV4_DST, "192.168.0.3/32")
					), 
					Arrays.asList(
						new FlowInstruction(FlowInstructionType.OUTPUT, "2")
					)
				));
			
			//DHCP Broadcast (->)
			elementFlows.add(new Flow(
				Arrays.asList(
						new FlowSelector(FlowSelectorType.ETH_TYPE, "0x0800"), 
						new FlowSelector(FlowSelectorType.IPV4_DST, "255.255.255.255/32"),
						new FlowSelector(FlowSelectorType.IP_PROTO, "17"),
						new FlowSelector(FlowSelectorType.UDP_DST, "67") 
				), 
				Arrays.asList(
					new FlowInstruction(FlowInstructionType.OUTPUT, "1")
				)
			));
			
			
			//DHCP Broadcast (<-) : TODO Loop Avoidance
			elementFlows.add(new Flow(
				Arrays.asList(
						new FlowSelector(FlowSelectorType.ETH_TYPE, "0x0800"), 
						new FlowSelector(FlowSelectorType.IPV4_SRC, "192.168.0.1/32"),
						new FlowSelector(FlowSelectorType.IPV4_DST, "192.168.0.255/32"),
						new FlowSelector(FlowSelectorType.IP_PROTO, "17"),
						new FlowSelector(FlowSelectorType.UDP_DST, "68") 
				), 
				Arrays.asList(
					new FlowInstruction(FlowInstructionType.FLOOD)
				)
			));
			
			
//			SNMP, OVSDB, Openflow
			//??
			
			onosManager.configureFlows(element, elementFlows);
		}
	}*/
}
