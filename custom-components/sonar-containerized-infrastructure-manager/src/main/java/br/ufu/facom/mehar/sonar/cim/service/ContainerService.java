package br.ufu.facom.mehar.sonar.cim.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.cim.exception.ContainerAlreadyRunningException;
import br.ufu.facom.mehar.sonar.cim.exception.ContainerInvalidException;
import br.ufu.facom.mehar.sonar.cim.exception.ContainerMismatchException;
import br.ufu.facom.mehar.sonar.cim.exception.ContainerNotFoundException;
import br.ufu.facom.mehar.sonar.cim.manager.ContainerManager;
import br.ufu.facom.mehar.sonar.cim.util.DataTranslator;
import br.ufu.facom.mehar.sonar.core.model.container.Container;
import br.ufu.facom.mehar.sonar.core.model.container.ContainerStatus;

@Service
public class ContainerService {
	@Autowired
	@Qualifier("docker")
	private ContainerManager containerManager;
	
	@Autowired
	private PortPoolService portPoolService;
	
	@Autowired
	private RegistryService registryService;
	
	private final static String AUTO_ASSIGN_PORT = "auto";
	
	private final static Boolean PREVENT_SIMILAR_CONTAINERS_WITHOUT_PORT_MAPPING = true;
	
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
		for(String server : registryService.get().getServers()) {
			containerResultList.addAll(this.getContainersByServer(server));
		}
		return containerResultList;
	}
	
	public List<Container> getContainersByNamespace(String namespace) {
		List<Container> containerResultList = new ArrayList<Container>();
		for(String server : registryService.get().getServers()) {
			containerResultList.addAll(this.getContainersByServerAndNamespace(server, namespace));
		}
		return containerResultList;
	}
	
	public List<Container> getContainersByNamespaceAndImage(String namespace, String image) {
		List<Container> containerResultList = new ArrayList<Container>();
		for(String server : registryService.get().getServers()) {
			containerResultList.addAll(this.getContainersByServerNamespaceAndImage(server, namespace, image));
		}
		return containerResultList;
	}
	
	public Container getContainerByContainerIdOrName(String idOrName) {
		for(String server : registryService.get().getServers()) {
			Container result = this.getContainerByServerAndContainerIdOrName(server, idOrName);
			if(result != null) {
				return result;
			}
		}
		return null;
	}
	
	/*
	 * GET Containers in a server
	 */
	public List<Container> getRunningContainersByServer(String server) {
		return DataTranslator.translate(containerManager.getRunningContainers(server), server);
	}
	
	public List<Container> getContainersByServer(String server) {
		return DataTranslator.translate(containerManager.getContainers(server), server);
	}

	public List<Container> getContainersByServerNamespaceAndImage(String server, String namespace, String image) {
		return DataTranslator.translate(containerManager.getContainersByImage(server, Container.getImageNameWithNamespace(namespace, image)), server);
	}
	
	public List<Container> getContainersByServerAndNamespace(String server, String namespace) {
		return DataTranslator.translate(containerManager.getContainersByNamespace(server, namespace), server);
	}

	public Container getContainerByServerAndContainerId(String server, String id) {
		return DataTranslator.translate(containerManager.getContainerById(server, id), server);
	}
	
	public Container getContainerByServerAndContainerIdOrName(String server, String idOrName) {
		Container container =  DataTranslator.translate(containerManager.getContainerById(server, idOrName), server);
		if(container == null) {
			return DataTranslator.translate(containerManager.getContainerByName(server, idOrName), server);
		}
		return container;
	}

	/*
	 * RUN METHODS 
	 */
	public Container run(Container container) {
		String server = container.getServer();
		String fullImageName = container.getImageNameWithNamespaceAndVersion();

		String containerId = container.getId();
		String containerName = container.getId(); 
		
		Boolean autoDestroy = container.getAutoDestroy();
		Boolean singleton = container.getSingleton();
		
	
		Map<String, String> portMapping = container.getPortMapping();
		List<String> entrypoint = container.getEntrypoint();
		List<String> cmd = container.getCmd();
		List<String> env = container.getEnv();
		Set<String> exposedPorts = container.getExposedPorts();
		Set<String> volumes = container.getVolumes();
		String network = container.getNetwork();
		
	
		if(containerId != null && !containerId.isEmpty()) {
			Container alreadyCreatedContainer = DataTranslator.translate(containerManager.getContainerById(server, containerId), server);
			if(alreadyCreatedContainer == null) {
				throw new ContainerNotFoundException("Can't find a container with id "+containerId+" in server "+server+".");
			}else {
				if(alreadyCreatedContainer.getStatus().equals(ContainerStatus.RUNNING_STATE)) {
					dealWithAlreadyRunningContainer(server, container, alreadyCreatedContainer, "Container with id "+containerId+" in server "+server+" is already running.");
				}else {
					return runContainer(container, server, alreadyCreatedContainer.getId());
				}
			}
		}
		
		if(containerName != null && !containerName.isEmpty()) {
			Container alreadyCreatedContainer = DataTranslator.translate(containerManager.getContainerByName(server, containerName), server);
			if(alreadyCreatedContainer == null) {
				throw new ContainerNotFoundException("Can't find a container with name "+containerName+" in server "+server+".");
			}else {
				if(alreadyCreatedContainer.getStatus().equals(ContainerStatus.RUNNING_STATE)) {
					dealWithAlreadyRunningContainer(server, container, alreadyCreatedContainer, "Container with name "+containerName+" in server "+server+" is already running.");
				}else {
					return runContainer(container, server, alreadyCreatedContainer.getId());
				}
			}
		}
		
		if(singleton != null && Boolean.TRUE.equals(singleton)) {
			List<Container> containers = DataTranslator.translate(containerManager.getContainersByImage(server, fullImageName), server);
			
			if(containers != null && !containers.isEmpty()) {
				for(Container alreadyCreatedContainer : containers) {
					if(alreadyCreatedContainer.getStatus().equals(ContainerStatus.RUNNING_STATE)) {
						dealWithAlreadyRunningContainer(server, container, alreadyCreatedContainer, "Container singleton with image "+fullImageName+" in server "+server+" is already running.");
					}
				}
				
				return runContainer(container, server, containers.get(0).getId());
			}
		}
		
		//Find Container with same port mapping
		List<Container> containers = DataTranslator.translate(containerManager.getContainersByImage(server, fullImageName), server);
		if(containers != null && !containers.isEmpty()) {
			for(Container alreadyCreatedContainer : containers) {
				if(portMapping != null && !portMapping.isEmpty()) {
					if(alreadyCreatedContainer.getPortMapping() != null && !alreadyCreatedContainer.getPortMapping().isEmpty()) {
						//Same Port-Mapping
						if(portMapping.equals(alreadyCreatedContainer.getPortMapping())) {
							//if already running
							if(alreadyCreatedContainer.getStatus().equals(ContainerStatus.RUNNING_STATE)) {
								dealWithAlreadyRunningContainer(server, container, alreadyCreatedContainer, "Container with image "+fullImageName+" with port-mapping "+alreadyCreatedContainer.getPortMapping()+" in server "+server+" is already running.");
							}else {
								return runContainer(container, server, alreadyCreatedContainer.getId());
							}
						}
					}
				}else {
					Set<String> accExposedPorts = alreadyCreatedContainer.getExposedPorts();
					if( ( (exposedPorts == null || exposedPorts.isEmpty()) && (accExposedPorts == null || accExposedPorts.isEmpty()) ) || (exposedPorts != null && exposedPorts.equals(accExposedPorts))) {
						if(PREVENT_SIMILAR_CONTAINERS_WITHOUT_PORT_MAPPING) {
							//if neither has port mapping
							if(alreadyCreatedContainer.getPortMapping() == null || alreadyCreatedContainer.getPortMapping().isEmpty()) {
								//if already running
								if(alreadyCreatedContainer.getStatus().equals(ContainerStatus.RUNNING_STATE)) {
									dealWithAlreadyRunningContainer(server, container, alreadyCreatedContainer, "Container with image "+fullImageName+" without port-mapping in server "+server+" is already running.");
								}
							}
						}
					}
				}
			}
		}
		
		List<Integer> portsAllocated = new ArrayList<Integer>();
		try {
			//Allocate Ports
			if(portMapping != null && !portMapping.isEmpty()) {
				for(String inPort : portMapping.keySet()) {
					if(portMapping.get(inPort) == null || portMapping.get(inPort).equalsIgnoreCase(AUTO_ASSIGN_PORT)){
						Integer outPort = portPoolService.allocatePort(server);
						portsAllocated.add(outPort);
						portMapping.put(inPort, outPort.toString());
					}else {
						Integer outPort = Integer.parseInt(inPort);
						portsAllocated.add(outPort);
						portPoolService.blockPort(server, outPort);
					}
				}
			}
			
			//Create And Run Container
			Container runningContainer = DataTranslator.translate(containerManager.runContainer(server, fullImageName, containerName, portMapping, exposedPorts, env, volumes, entrypoint, cmd, autoDestroy, network), server);
			container = merge(container, runningContainer);

			registryService.register(container);
			
			return container;
			
		}catch(RuntimeException e) {
			//release ports
			for(Integer port : portsAllocated) {
				portPoolService.releasePort(server, port);
			}
			
			throw e;
		}
	}

	private void dealWithAlreadyRunningContainer(String server, Container container, Container alreadyCreatedContainer, String message) {
		registryService.register(merge(container, alreadyCreatedContainer));
		if(alreadyCreatedContainer.getPortMapping() != null && !alreadyCreatedContainer.getPortMapping().isEmpty()) {
			portPoolService.blockPorts(server, alreadyCreatedContainer.getPortMapping().values());
		}
		throw new ContainerAlreadyRunningException(message);
	}

	private Container runContainer(Container container, String server, String containerId) {
		Container runningContainer =  DataTranslator.translate(containerManager.runContainerById(server, containerId), server);
		
		container = merge(container, runningContainer);

		registryService.register(container);
		
		return container;
	}

	private Container merge(Container container, Container runningContainer) {
		container.setId(runningContainer.getId());
		container.setName(runningContainer.getName());
		container.setStatus(runningContainer.getStatus());
		container.setPortMapping(runningContainer.getPortMapping());
		container.setNamespace(runningContainer.getNamespace());
		container.setImage(runningContainer.getImage());
		container.setVersion(runningContainer.getVersion());
		return container;
	}
	
	public Container run(String server, String namespace, String image) {
		Container container = new Container();
		container.setServer(server);
		container.setNamespace(namespace);
		container.setImage(image);
		container.setAutoDestroy(Boolean.TRUE);
		container.setSingleton(Boolean.TRUE);
		
		return this.run(container);
	}

	public Container run(String server, String namespace, String image, String idOrName) {
		Container container = this.getContainerByContainerIdOrName(idOrName);
		
		if(container != null) {
			String requestImage = Container.getImageNameWithNamespace(namespace, image);
			String foundImageName = container.getImageNameWithNamespace();
			if(requestImage != null && foundImageName != null && !requestImage.equals(foundImageName)) {
				throw new ContainerMismatchException("Container with id or name ("+idOrName+") is based on "+foundImageName+" and not on requested  "+requestImage+".");
			}
			
			return this.run(container);
			
		}else {
			throw new ContainerNotFoundException("Can't find a container with id or name "+idOrName+" in server "+server+".");
		}
		
	}
	
	public Container run(String server, String idOrName) {
		Container container = this.getContainerByContainerIdOrName(idOrName);
		
		if(container != null) {
			return this.run(container);
		}else {
			throw new ContainerNotFoundException("Can't find a container with id or name "+idOrName+" in server "+server+".");
		}
	}

	/*
	 * STOP METHODS 
	 */
	public Container stop(String server, String namespace, String image, String idOrName) {
		Container container = this.getContainerByContainerIdOrName(idOrName);
		
		if(container != null) {
			String requestImage = Container.getImageNameWithNamespace(namespace, image);
			String foundImageName = container.getImageNameWithNamespace();
			if(requestImage != null && foundImageName != null && !requestImage.equals(foundImageName)) {
				throw new ContainerMismatchException("Container with id or name ("+idOrName+") is based on "+foundImageName+" and not on requested  "+requestImage+".");
			}
			
			return this.stop(container);
			
		}else {
			throw new ContainerNotFoundException("Can't find a container with id or name "+idOrName+" in server "+server+".");
		}
	}
	
	public Container stop(String server, String idOrName) {
		Container container = this.getContainerByContainerIdOrName(idOrName);
		
		if(container != null) {
			return this.stop(container);
			
		}else {
			throw new ContainerNotFoundException("Can't find a container with id or name "+idOrName+" in server "+server+".");
		}
	}
	
	public List<Container> stop(String server, String namespace, String image) {
		List<Container> resultList = new ArrayList<Container>();
		
		List<Container> containerList = this.getContainersByServerNamespaceAndImage(server, namespace, image);
		if(containerList != null && !containerList.isEmpty()) {
			for(Container container : containerList) {
				resultList.add(this.stop(container));
			}
		}
		
		return resultList;
	}

	public Container stop(Container container) {
		if(container != null && container.getServer() != null && container.getId() != null) {
			Container actualContainer = this.getContainerByServerAndContainerId(container.getServer(), container.getId());
			
			if(actualContainer == null) {
				throw new ContainerNotFoundException("Can't find a container with id "+container.getId()+" in server "+container.getServer()+".");
			}else {
				if(actualContainer.getStatus().equals(ContainerStatus.RUNNING_STATE)) {
					containerManager.stopContainer(container.getServer(), container.getId(), container.getAutoDestroy());
				}else {
					containerManager.deleteContainer(container.getServer(), container.getId());
				}	
					
				if(Boolean.TRUE.equals(container.getAutoDestroy())){
					container.setStatus(ContainerStatus.REMOVED_STATE);
					
					if(container.getPortMapping() != null && !container.getPortMapping().isEmpty()) {
						for(String outPort : container.getPortMapping().values()) {
							portPoolService.releasePort(container.getServer(), Integer.parseInt(outPort));
						}
					}
					registryService.unregister(container);
				}else {
					container.setStatus(ContainerStatus.STOPPED_STATE);
				}
				
				return container;
			}
		}else {
			throw new ContainerInvalidException("Container requested to stop is invalid! "+container);
		}
	}
}
