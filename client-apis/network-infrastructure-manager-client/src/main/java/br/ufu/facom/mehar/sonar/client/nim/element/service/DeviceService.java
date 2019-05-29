package br.ufu.facom.mehar.sonar.client.nim.element.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.client.nim.element.manager.impl.ONOSManager;
import br.ufu.facom.mehar.sonar.client.nim.element.manager.impl.OVSDBManager;
import br.ufu.facom.mehar.sonar.core.model.configuration.Configuration;
import br.ufu.facom.mehar.sonar.core.model.configuration.ConfigurationType;
import br.ufu.facom.mehar.sonar.core.model.configuration.Flow;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;

@Service
public class DeviceService {

	private Logger logger = LoggerFactory.getLogger(DeviceService.class);

	@Autowired
	private OVSDBManager ovsdbManager;
	
	@Autowired
	private ONOSManager onosManager;
	
	public void configureControllerIfSupported(Element element, String[] controllers) {
		//TODO Verify Vendor/Model
		ovsdbManager.configureController(element, controllers);
	}

	public void configure(Element element, List<Configuration> configurationList) {
		//TODO Verify Vendor/Model and Type of Configuration
		
		List<Flow> flowList = new ArrayList<Flow>();
		for(Configuration configuration : configurationList) {
			if(ConfigurationType.FLOW_CREATION.equals(configuration.getType())) {
				flowList.add(configuration.getFlow());
			}
		}
		
		if(element.getOfDeviceId() != null) {
			onosManager.configureFlows(element, flowList);
		}
	}
}
