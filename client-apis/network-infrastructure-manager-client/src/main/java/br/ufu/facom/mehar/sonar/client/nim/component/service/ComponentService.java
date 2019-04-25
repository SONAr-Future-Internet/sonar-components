package br.ufu.facom.mehar.sonar.client.nim.component.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.ufu.facom.mehar.sonar.client.nim.component.Component;
import br.ufu.facom.mehar.sonar.client.nim.component.exception.ComponentNotFoundException;
import br.ufu.facom.mehar.sonar.client.nim.component.exception.ComponentsNotLoadedException;
import br.ufu.facom.mehar.sonar.client.nim.component.exception.UnableToRunComponentException;
import br.ufu.facom.mehar.sonar.client.nim.component.exception.UnableToStopComponentException;
import br.ufu.facom.mehar.sonar.client.nim.component.manager.ContainerManager;
import br.ufu.facom.mehar.sonar.core.model.container.Container;
import br.ufu.facom.mehar.sonar.core.util.ObjectUtils;

@Service
public class ComponentService {
	private Logger logger = LoggerFactory.getLogger(ComponentService.class);

	@Autowired
	@Qualifier("sonar-cim")
	private ContainerManager containerManager;
	
	private Map<String, Container> componentMap;
	
	public ComponentService() {
		try {
			InputStream inputStream = new ClassPathResource("sonar-components-configuration.json").getInputStream();

			ObjectMapper objectMapper = new ObjectMapper();
			
			componentMap =  objectMapper.readValue(inputStream, new TypeReference<Map<String, Container>>(){});
		} catch (IOException e) {
			logger.error("Unable to load sonar components descriptor file : 'sonar-components-configuration.json' in resources.", e);
		}
	}
	
	public Container runComponent(String managerIp, Component component){
		return this.runComponent(managerIp, component, "local");
	}
	
	public Container runComponent(String managerIp, Component component, String server){
		return this.runComponent(managerIp, component, server, new Properties());
	}
	
	public Container runComponent(String managerIp, Component component, Properties properties) {
		return this.runComponent(managerIp, component, "local", properties);
	}

	public Container runComponent(String managerIp, Component component, String server, Properties properties){
		//Components loaded?
		if(componentMap == null) {
			throw new ComponentsNotLoadedException("There was a problem while loading sonar components... read log for more details.");
		}
		
		//Component found?
		Container containerOnConfiguration = componentMap.get(component.getKey());
		if(containerOnConfiguration == null) {
			throw new ComponentNotFoundException("Component specification "+component.toString()+" not found.");
		}
		
		//Cloning container
		Container container = ObjectUtils.clone(containerOnConfiguration);
		
		//Setting 'server'
		container.setServer(server);
		
		//Setting 'env'
		if(!properties.isEmpty()) {
			if(container.getEnv() == null) {
				container.setEnv(new ArrayList<String>());
			}
			for(Object propKey : properties.keySet()) {
				container.getEnv().add(propKey.toString()+"="+properties.getProperty(propKey.toString()));
			}
		}
		
		try {
		//Run
		Container runningContainer = containerManager.run(managerIp, container);
		
		if(runningContainer != null && runningContainer.getId() != null) {
			return runningContainer;
		}else {
			throw new UnableToRunComponentException("Return of manager for running the component "+component+" is invalid.");
		}
		}catch(Exception e) {
			throw new UnableToRunComponentException("Error while trying to run the component "+component+".", e);
		}
	}
	
	public Container stopComponent(String managerIp, String idContainer, Component component){
		return this.stopComponent(managerIp, idContainer, component, "local");
	}
	
	public Container deleteComponent(String managerIp, String idContainer, Component component) {
		return this.stopComponent(managerIp, idContainer, component, "local", Boolean.TRUE);
	}
	
	public Container stopComponent(String managerIp, String idContainer, Component component, String server){
		return this.stopComponent(managerIp, idContainer, component, server, Boolean.FALSE);
	}
	
	public Container stopComponent(String managerIp, String idContainer, Component component, String server, Boolean forceAutoDelete){
		//Components loaded?
		if(componentMap == null) {
			throw new ComponentsNotLoadedException("There was a problem while loading sonar components... read log for more details.");
		}
		
		//Component found?
		Container containerOnConfiguration = componentMap.get(component.getKey());
		if(containerOnConfiguration == null) {
			throw new ComponentNotFoundException("Component specification "+component.toString()+" not found.");
		}
		
		//Cloning container
		Container container = ObjectUtils.clone(containerOnConfiguration);
		
		//Setting 'server'
		container.setServer(server);
		container.setId(idContainer);
		
		if(forceAutoDelete) {
			container.setAutoDestroy(Boolean.TRUE);
		}
		
		try {
		//Run
		Container runningContainer = containerManager.stop(managerIp, container);
		
		if(runningContainer != null && runningContainer.getId() != null) {
			return generateContainerInfo(runningContainer, containerOnConfiguration, managerIp);
		}else {
			throw new UnableToStopComponentException("Return of manager for running the component "+component+" is invalid.");
		}
		}catch(Exception e) {
			throw new UnableToStopComponentException("Error while trying to run the component "+component+".", e);
		}
	}
	
	public Map<Component, List<Container>> get(String managerIp){
		Map<Component, List<Container>> result = new HashMap<Component, List<Container>>();
		
		//Components loaded?
		if(componentMap == null) {
			throw new ComponentsNotLoadedException("There was a problem while loading sonar components... read log for more details.");
		}
		
		//Get all components	
		List<Container> containerList = containerManager.get(managerIp);
		if(containerList != null && !containerList.isEmpty()) {
			for(Container container : containerList) {
				//Identify component
				Component component = findComponent(container);
				if(component != null) {
					if(!result.containsKey(component)) {
						result.put(component, new ArrayList<Container>());
					}
					
					Container containerOnConfiguration = componentMap.get(component.getKey());
					if(containerOnConfiguration != null) {
						result.get(component).add(generateContainerInfo(container, containerOnConfiguration , managerIp));
					}
				}
			}
		}
		
		return result;
	}

	private Component findComponent(Container target) {
		for(String componentKey : componentMap.keySet()) {
			Container container = componentMap.get(componentKey);
			if(container.equals(target)) {
				return Component.getByKey(componentKey);
			}
		}
		return null;
	}

	private Container generateContainerInfo(Container containerOnManager, Container containerOnConfiguration, String managerIp) {
		Container result = ObjectUtils.clone(containerOnConfiguration);
		
		if("local".equals(containerOnManager.getServer())) {
			result.setServer(managerIp);
		}else {
			result.setServer(containerOnManager.getServer());
		}
		
		result.setId(containerOnManager.getId());
		result.setStatus(containerOnManager.getStatus());
		
		return result;
	}
}
