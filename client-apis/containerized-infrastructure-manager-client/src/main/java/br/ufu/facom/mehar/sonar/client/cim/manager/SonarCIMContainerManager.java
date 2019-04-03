package br.ufu.facom.mehar.sonar.client.cim.manager;

import java.util.List;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import br.ufu.facom.mehar.sonar.core.model.container.Container;

@Component("sonar-cim")
public class SonarCIMContainerManager implements ContainerManager{
	Logger logger = Logger.getLogger(SonarCIMContainerManager.class);
	
	@Value("${cim.manager.port:8080}")
	private String SONAR_CIM_PORT;

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public Container run(String managerIp, Container container) {
		logger.info(this.getClass().getCanonicalName() + " -> run");
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		
		ResponseEntity<Container> response = restTemplate.exchange(
				"http://"+managerIp+":"+SONAR_CIM_PORT+"/api/v1/container",
				HttpMethod.POST, new HttpEntity<>(container, headers),
				new ParameterizedTypeReference<Container>() {
				});

		return response.getBody();
	}

	@Override
	public Container stop(String managerIp, Container container) {
		logger.info(this.getClass().getCanonicalName() + " -> stop");
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

		ResponseEntity<Container> response = restTemplate.exchange(
				"http://"+managerIp+":"+SONAR_CIM_PORT+"/api/v1/container",
				HttpMethod.DELETE, new HttpEntity<>(container, headers),
				new ParameterizedTypeReference<Container>() {
				});

		return response.getBody();
	}

	@Override
	public List<Container> get(String managerIp){
		logger.info(this.getClass().getCanonicalName() + " -> get");

		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		
		ResponseEntity<List<Container>> response = restTemplate.exchange(
				"http://"+managerIp+":"+SONAR_CIM_PORT+"/api/v1/container",
				HttpMethod.GET, new HttpEntity<>(headers),
				new ParameterizedTypeReference<List<Container>>() {
				});

		return response.getBody();
	}
}
