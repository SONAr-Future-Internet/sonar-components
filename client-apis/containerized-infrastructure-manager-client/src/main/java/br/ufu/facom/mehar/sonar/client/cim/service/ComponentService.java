package br.ufu.facom.mehar.sonar.client.cim.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.ufu.facom.mehar.sonar.client.cim.Component;
import br.ufu.facom.mehar.sonar.client.cim.exception.ComponentNotFoundException;
import br.ufu.facom.mehar.sonar.client.cim.exception.ComponentsNotLoadedException;
import br.ufu.facom.mehar.sonar.client.cim.exception.UnableToRunComponentException;
import br.ufu.facom.mehar.sonar.client.cim.exception.UnableToStopComponentException;
import br.ufu.facom.mehar.sonar.client.cim.manager.ContainerManager;
import br.ufu.facom.mehar.sonar.core.model.container.Container;
import br.ufu.facom.mehar.sonar.core.util.ObjectUtils;

@Service
public class ComponentService {
	private Logger logger = Logger.getLogger(ComponentService.class);

	@Autowired
	@Qualifier("sonar-cim")
	private ContainerManager containerManager;
	
	private Map<String, Container> componentMap;
	
	public ComponentService() {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("sonar-components-configuration.json").getFile());

		ObjectMapper objectMapper = new ObjectMapper();
		
		try {
			componentMap =  objectMapper.readValue(file, new TypeReference<Map<String, Container>>(){});
		} catch (IOException e) {
			logger.fatal("Unable to load sonar components descriptor file : 'sonar-components-configuration.json' in resources.", e);
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
	
	public Container stopComponent(String managerIp, Component component){
		return this.stopComponent(managerIp, component, "local");
	}
	
	public Container deleteComponent(String managerIp, Component component) {
		return this.stopComponent(managerIp, component, "local", Boolean.TRUE);
	}
	
	public Container stopComponent(String managerIp, Component component, String server){
		return this.stopComponent(managerIp, component, server, Boolean.FALSE);
	}
	
	public Container stopComponent(String managerIp, Component component, String server, Boolean forceAutoDelete){
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
	
	public Map<Component, Container> get(String managerIp){
		Map<Component, Container> result = new HashMap<Component, Container>();
		
		//Components loaded?
		if(componentMap == null) {
			throw new ComponentsNotLoadedException("There was a problem while loading sonar components... read log for more details.");
		}
		
		Map<Container, Component> reverseComponentMap = new HashMap<Container, Component>();
		for(String key : componentMap.keySet()) {
			Component component = Component.getByKey(key);
			if(component != null) {
				reverseComponentMap.put(componentMap.get(key), component);
			}
		}
		
		List<Container> containerList = containerManager.get(managerIp);
		if(containerList != null && !containerList.isEmpty()) {
			for(Container container : containerList) {
				Component component = reverseComponentMap.get(container);
				if(component != null) {
					result.put(component, generateContainerInfo(container, componentMap.get(component), managerIp));
				}
			}
		}
		
		return result;
	}

	private Container generateContainerInfo(Container containerOnManager, Container containerOnConfiguration, String managerIp) {
		Container result = ObjectUtils.clone(containerOnConfiguration);
		
		if("local".equals(containerOnConfiguration.getServer())) {
			result.setServer(managerIp);
		}else {
			result.setServer(containerOnConfiguration.getServer());
		}
		
		result.setId(containerOnConfiguration.getId());
		result.setStatus(containerOnConfiguration.getStatus());
		
		return result;
	}
}
