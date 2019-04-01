package br.ufu.facom.mehar.sonar.cim.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.cim.exception.ContainerAlreadyRunningException;
import br.ufu.facom.mehar.sonar.cim.exception.ContainerNotFoundException;
import br.ufu.facom.mehar.sonar.cim.util.DataTranslator;
import br.ufu.facom.mehar.sonar.core.model.container.Container;
import br.ufu.facom.mehar.sonar.core.model.container.Registry;

@Service
public class ContainerService {
	@Autowired
	private DockerService dockerService;
	
	@Autowired
	private PortPoolService portPoolService;
	
	@Autowired
	private RegistryService registryService;
	
	private final static String AUTO_ASSIGN_PORT = "auto";
	
	/*
	 * Servers
	 */
	public Boolean registerServer(String server) {
		registryService.registerServer(server);
		return Boolean.TRUE;
	}
	
	public Boolean unregisterServer(String server) {
		registryService.unregisterServer(server);
		return Boolean.TRUE;
	}
	
	/*
	 * GET Containers in all servers
	 */
	public List<Container> getContainers() {
		List<Container> containerResultList = new ArrayList<Container>();
		for(Registry.Server server : registryService.get().getServers()) {
			containerResultList.addAll(this.getContainersByServer(server.getId()));
		}
		return containerResultList;
	}
	
	public List<Container> getContainersByNamespace(String namespace) {
		List<Container> containerResultList = new ArrayList<Container>();
		for(Registry.Server server : registryService.get().getServers()) {
			containerResultList.addAll(this.getContainersByServerAndNamespace(server.getId(), namespace));
		}
		return containerResultList;
	}
	
	public List<Container> getContainersByNamespaceAndImage(String namespace, String image) {
		List<Container> containerResultList = new ArrayList<Container>();
		for(Registry.Server server : registryService.get().getServers()) {
			containerResultList.addAll(this.getContainersByServerNamespaceAndImage(server.getId(), namespace, image));
		}
		return containerResultList;
	}
	
	public Container getContainerByContainerIdOrName(String idOrName) {
		for(Registry.Server server : registryService.get().getServers()) {
			Container result = this.getContainerByServerAndContainerIdOrName(server.getId(), idOrName);
			if(result != null) {
				return result;
			}
		}
		return null;
	}
	
	/*
	 * GET Containers in a server
	 */
	public List<Container> getContainersByServer(String server) {
		return DataTranslator.convertDockerContainerToGenericContainer(dockerService.getContainers(server), server);
	}

	public List<Container> getContainersByServerNamespaceAndImage(String server, String namespace, String image) {
		return DataTranslator.convertDockerContainerToGenericContainer(dockerService.getContainersByImage(server, this.generateImageName(namespace, image)), server);
	}
	
	public List<Container> getContainersByServerAndNamespace(String server, String namespace) {
		return DataTranslator.convertDockerContainerToGenericContainer(dockerService.getContainersByNamespace(server, namespace), server);
	}

	public Container getContainerByServerAndContainerId(String server, String id) {
		return DataTranslator.convertDockerContainerToGenericContainer(dockerService.getContainerById(server, id), server);
	}
	
	public Container getContainerByServerAndContainerIdOrName(String server, String idOrName) {
		Container container =  DataTranslator.convertDockerContainerToGenericContainer(dockerService.getContainerById(server, idOrName), server);
		if(container == null) {
			return DataTranslator.convertDockerContainerToGenericContainer(dockerService.getContainerByName(server, idOrName), server);
		}
		return container;
	}
	
	/*
	 * UTIL 
	 */
	private String generateImageName(String namespace, String imageName) {
		return namespace != null? namespace + "/" +imageName : imageName;
	}

	public Container run(Container container) {
		String server = container.getServer();
		String containerId = container.getId();
		String containerName = container.getId(); 
		Boolean autoDestroy = container.getAutoDestroy();
		Boolean singleton = container.getSingleton();
		Map<String, String> portMapping = container.getPortMapping();
		String fullImageName = this.generateImageName(container.getNamespace(), container.getImageName());
				
				
		if(containerId != null && !containerId.isEmpty()) {
			Container alreadyCreatedContainer = DataTranslator.convertDockerContainerToGenericContainer(dockerService.getContainerById(server, containerId), server);
			if(alreadyCreatedContainer == null) {
				throw new ContainerNotFoundException("Can't find a container with id "+containerId+" in server "+server+".");
			}else {
				if(alreadyCreatedContainer.getStatus().equals(DockerService.RUNNING_STATE)) {
					throw new ContainerAlreadyRunningException("Container with id "+containerId+" in server "+server+" is already running.");
				}else {
					return DataTranslator.convertDockerContainerToGenericContainer(dockerService.runContainerById(alreadyCreatedContainer.getId()), server);
				}
			}
		}
		
		if(containerName != null && !containerName.isEmpty()) {
			Container alreadyCreatedContainer = DataTranslator.convertDockerContainerToGenericContainer(dockerService.getContainerByName(server, containerName), server);
			if(alreadyCreatedContainer == null) {
				throw new ContainerNotFoundException("Can't find a container with name "+containerName+" in server "+server+".");
			}else {
				if(alreadyCreatedContainer.getStatus().equals(DockerService.RUNNING_STATE)) {
					throw new ContainerAlreadyRunningException("Container with name "+containerName+" in server "+server+" is already running.");
				}else {
					return DataTranslator.convertDockerContainerToGenericContainer(dockerService.runContainerById(alreadyCreatedContainer.getId()), server);
				}
			}
		}
		
		if(singleton) {
			List<Container> containers = DataTranslator.convertDockerContainerToGenericContainer(dockerService.getContainersByImage(server, fullImageName), server);
			
			if(containers != null && !containers.isEmpty()) {
				for(Container alreadyCreatedContainer : containers) {
					if(alreadyCreatedContainer.getStatus().equals(DockerService.RUNNING_STATE)) {
						throw new ContainerAlreadyRunningException("Container singleton with image "+fullImageName+" in server "+server+" is already running.");
					}
				}
			}
			//Run the first Container
			return DataTranslator.convertDockerContainerToGenericContainer(dockerService.runContainerById(containers.get(0).getId()),server);
				
		}
		
		//Allocate Ports
		if(container.getPortMapping() != null && !container.getPortMapping().isEmpty()) {
			for(String inPort : portMapping.keySet()) {
				if(portMapping.get(inPort) == null || portMapping.get(inPort).equalsIgnoreCase(AUTO_ASSIGN_PORT)){
					portMapping.put(inPort, portPoolService.allocatePort(container.getServer()).toString());
				}else {
					portPoolService.allocatePort(container.getServer(), Integer.parseInt(inPort));
				}
			}
		}
		
//		String id = dockerService.runContainer(server, fullImageName, containerName, portMapping, autoDestroy);
//		container.setId(id);
//		container.setStatus(DockerService.RUNNING_STATE);
		
		registryService.register(container);
		
		return container;
	}

	public Container run(String server, String namespace, String image) {
		// TODO Auto-generated method stub
		return null;
	}

	public Container run(String server, String namespace, String image, String idOrName) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Container> stop(String server, String namespace, String image, String idOrName) {
		// TODO Auto-generated method stub
		return null;
	}

	public Container stop(Container container) {
		// TODO Auto-generated method stub
		return null;
	}

	public Container stopByIdOrName(String server, String namespace, String image, String idOrName) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * Run
	 */
}
