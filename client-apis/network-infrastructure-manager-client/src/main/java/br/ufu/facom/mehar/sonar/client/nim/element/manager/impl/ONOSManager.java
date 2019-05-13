package br.ufu.facom.mehar.sonar.client.nim.element.manager.impl;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
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
import br.ufu.facom.mehar.sonar.core.model.topology.Element;

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
	public void configureFlows(Element element, Set<Flow> flows, Boolean permanent) {
		ONOSFlowGroup group = ElementModelTranslator.convertToONOSFlow(element, flows, permanent);
	}

	@Override
	public Element discover(String ip) {
		throw new MethodNotImplementedYetException("Method 'discover' of ONOSManager not implemented yet!");
	}

	@Override
	public void configureController(Element element, String[] controllerTargets) {
		throw new MethodNotSupportedException("Method 'configureController' of ONOSManager not suported!");
	}
}
