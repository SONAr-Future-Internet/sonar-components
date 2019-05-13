package br.ufu.facom.mehar.sonar.client.nim.element.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.client.nim.element.manager.impl.SNMPManager;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;

@Service
public class ElementService {
	private Logger logger = LoggerFactory.getLogger(ElementService.class);

	@Autowired
	private SNMPManager elementManager;
	
	public Element discover(String ip) {
		return elementManager.discover(ip);
	}
}
