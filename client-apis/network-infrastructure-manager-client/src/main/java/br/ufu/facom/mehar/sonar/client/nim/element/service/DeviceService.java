package br.ufu.facom.mehar.sonar.client.nim.element.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.client.nim.element.manager.OVSDBManager;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;

@Service
public class DeviceService {

	private Logger logger = LoggerFactory.getLogger(DeviceService.class);

	@Autowired
	private OVSDBManager ovsdbManager;
	
	public void configureControllerIfSupported(Element element, String[] controllers) {
		//TODO Verify Vendor/Model
		ovsdbManager.configureController(element.getManagementIPAddressList().iterator().next(), controllers);
	}
}
