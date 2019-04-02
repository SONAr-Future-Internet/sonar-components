package br.ufu.facom.mehar.sonar.client.cim.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.client.cim.SONArComponents;

@Service
public class SONArComponentManager {
	
	@Value("${cim.sonar.nddb.image:meharsonar/cassandra:latest}")
	private String nddbImage;
	
	@Value("${cim.sonar.nem.image:meharsonar/rabbitmq:latest}")
	private String nemImage;

	@Autowired
	private ContainerManager containerManager;
	
	private void runComponent(SONArComponents component, String server){
//		switch
	}
}
