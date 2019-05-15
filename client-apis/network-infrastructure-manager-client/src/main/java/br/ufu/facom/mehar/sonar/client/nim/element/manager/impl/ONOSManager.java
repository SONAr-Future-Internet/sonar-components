package br.ufu.facom.mehar.sonar.client.nim.element.manager.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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

import br.ufu.facom.mehar.sonar.client.nim.component.configuration.NIMConfiguration;
import br.ufu.facom.mehar.sonar.client.nim.component.manager.SonarCIMContainerManager;
import br.ufu.facom.mehar.sonar.client.nim.element.exception.MethodNotImplementedYetException;
import br.ufu.facom.mehar.sonar.client.nim.element.exception.MethodNotSupportedException;
import br.ufu.facom.mehar.sonar.client.nim.element.manager.SDNManager;
import br.ufu.facom.mehar.sonar.client.nim.element.model.ElementModelTranslator;
import br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow.ONOSDevice;
import br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow.ONOSDiscovery;
import br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow.ONOSFlowGroup;
import br.ufu.facom.mehar.sonar.core.model.configuration.Flow;
import br.ufu.facom.mehar.sonar.core.model.configuration.FlowInstruction;
import br.ufu.facom.mehar.sonar.core.model.configuration.FlowInstructionType;
import br.ufu.facom.mehar.sonar.core.model.configuration.FlowSelector;
import br.ufu.facom.mehar.sonar.core.model.configuration.FlowSelectorType;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;
import br.ufu.facom.mehar.sonar.core.util.ObjectUtils;

@Component("onos")
public class ONOSManager implements SDNManager {
	Logger logger = LoggerFactory.getLogger(SonarCIMContainerManager.class);
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private NIMConfiguration configuration;
	

	@Override
	public Collection<Element> discover() {
		logger.debug(this.getClass().getCanonicalName() + "Get all devices on manager ONOS ["+configuration.getSDNNorthSeeds()+"]");
		Map<String, Element> responseMap = new HashMap<String, Element>();
		for(String endpointController : configuration.getSDNNorthSeeds()) {
			try {
				HttpHeaders headers = new HttpHeaders();
				headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
				headers.set("Authorization", "Basic " + Base64.encodeBase64String( configuration.getSdnNorthAuthString().getBytes(StandardCharsets.UTF_8)).trim() );
				
				ResponseEntity<ONOSDiscovery> responseDeviceList = restTemplate.exchange(
						"http://"+endpointController+"/onos/v1/devices",
						HttpMethod.GET, new HttpEntity<>(headers),
						new ParameterizedTypeReference<ONOSDiscovery>() {
						});
		
				ONOSDiscovery discovery = responseDeviceList.getBody();
				if(discovery != null && discovery.getDevices() != null && !discovery.getDevices().isEmpty()) {
					for(ONOSDevice onosDevice : discovery.getDevices()) {
						if(!responseMap.containsKey(onosDevice.getId())) {
							//Get all device information and ports
							ResponseEntity<ONOSDevice> responseDevice = restTemplate.exchange(
									"http://"+endpointController+"/onos/v1/devices/"+onosDevice.getId()+"/ports",
									HttpMethod.GET, new HttpEntity<>(headers),
									new ParameterizedTypeReference<ONOSDevice>() {
									});
							
							ONOSDevice completeOnosDevice = responseDevice.getBody();
							responseMap.put(completeOnosDevice.getId(), ElementModelTranslator.convertToElement(completeOnosDevice));
						}
					}
				}
			} catch(Exception e) {
				logger.error("Error discovering devices on ONOS Controller with endpoint:"+endpointController, e);
			}
		}
		return responseMap.values();
	}
	
	@Override
	public void configureFlows(Element element, List<Flow> flows) {
		ONOSFlowGroup group = ElementModelTranslator.convertToONOSFlow(element, flows);
		
		for(String endpointController : configuration.getSDNNorthSeeds()) {
			try {
				HttpHeaders headers = new HttpHeaders();
				headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
				headers.set("Authorization", "Basic " + Base64.encodeBase64String( configuration.getSdnNorthAuthString().getBytes(StandardCharsets.UTF_8)).trim() );
				
				System.out.println(ObjectUtils.fromObject(group));
				
				ResponseEntity<String> responseDeviceList = restTemplate.exchange(
						"http://"+endpointController+"/onos/v1/flows",
						HttpMethod.POST, new HttpEntity<>(group, headers),
						new ParameterizedTypeReference<String>() {
						});
		
				String result = responseDeviceList.getBody();
				
				System.out.println(result);
				
				break;
				
			} catch(Exception e) {
				logger.error("Error pushing flows on ONOS Controller with endpoint:"+endpointController+" for element:"+ObjectUtils.toString(element), e);
			}
		}
	}

	@Override
	public Element discover(String ip) {
		throw new MethodNotImplementedYetException("Method 'discover' of ONOSManager not implemented yet!");
	}

	@Override
	public void configureController(Element element, String[] controllerTargets) {
		throw new MethodNotSupportedException("Method 'configureController' of ONOSManager not suported!");
	}
	
	public static void main(String[] args) {
		ONOSManager onosManager = new ONOSManager();
		onosManager.restTemplate = new RestTemplate();
		onosManager.configuration = new NIMConfiguration();
		onosManager.configuration.setSDNNorthSeedsAttr("192.168.0.1:8181");
		onosManager.configuration.setSDNNorthAuthStringAttr("onos:rocks");
		
		List<Flow> flows =  new ArrayList<Flow>();
		//Generic ARP Flow
		//flows.add(new Flow(new FlowSelector(FlowSelectorType.ETH_TYPE, "0x0806"), new FlowInstruction(FlowInstructionType.NORMAL)));//
		
		//Generic LLDP Flow
		//flows.add(new Flow(new FlowSelector(FlowSelectorType.ETH_TYPE, "0x88CC"), new FlowInstruction(FlowInstructionType.NORMAL)));
		
		//Device specifics
		{
			Element element = new Element();
			element.setOfDeviceId("of:0000168dd144704a");
			element.setManagementIPAddressList(new HashSet<String>(Arrays.asList("192.168.0.2")));
			List<Flow> elementFlows = new ArrayList<Flow>(flows);
			
			//Route to Server
			/*
			elementFlows.add(new Flow(
				Arrays.asList(
					new FlowSelector(FlowSelectorType.ETH_TYPE, "0x0800"), 
					new FlowSelector(FlowSelectorType.IPV4_DST, "192.168.0.1/32")
				), 
				Arrays.asList(
					new FlowInstruction(FlowInstructionType.OUTPUT, "1")
				)
			));
			*/
			
			//Local Processing
			/*
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
			*/
			
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
			/*
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
			*/
			
			//DHCP Broadcast (<-)
			/*
			elementFlows.add(new Flow(
				Arrays.asList(
						new FlowSelector(FlowSelectorType.ETH_TYPE, "0x0800"), 
						new FlowSelector(FlowSelectorType.IPV4_DST, "192.168.0.255/32"),
						new FlowSelector(FlowSelectorType.IP_PROTO, "17"),
						new FlowSelector(FlowSelectorType.UDP_DST, "68") 
				), 
				Arrays.asList(
					new FlowInstruction(FlowInstructionType.FLOOD)
				)
			));
			*/
			
			
			onosManager.configureFlows(element, elementFlows);
		}
	}
}
