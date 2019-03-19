package br.ufu.facom.mehar.sonar.cim.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.cim.model.Container;
import br.ufu.facom.mehar.sonar.cim.util.DataTranslator;

@Service
public class ContainerService {
	@Autowired
	private DockerService dockerService;
	
	@Autowired
	private PortPoolService portPoolService;
	
	@Autowired
	private RegistryService registryService;
	
	private static final Integer DEFAULT_APP_PORT = 8080;

	public List<Container> getContainersByServerNamespaceAndImage(String server, String namespace, String image) {
		return DataTranslator.convertDockerContainerToGenericContainer(dockerService.getRunningContainersByImage(server, this.generateImageName(namespace, image)), server);
	}
	
	public List<Container> getContainersByServerAndNamespace(String server, String namespace) {
		return DataTranslator.convertDockerContainerToGenericContainer(dockerService.getRunningContainersByNamespace(server, namespace), server);
	}

	public Container getContainerByServerAndId(String server, String id) {
		return DataTranslator.convertDockerContainerToGenericContainer(dockerService.getContainerById(server, id), server);
	}

	public List<Container> stop(String server, String namespace, String image) {
		List<Container> list = DataTranslator.convertDockerContainerToGenericContainer(dockerService.stopContainer(server, this.generateImageName(namespace, image)), server);
		if(list != null && !list.isEmpty()) {
			for(Container container : list) {
				if(container.getOutPort() != null && !container.getOutPort().isEmpty()) {
					portPoolService.releasePort(server, Integer.parseInt(container.getOutPort()));
					registryService.unregister(container);
					container.setStatus(DockerService.STOPPED_STATE);
				}
			}
		}
		return list;
	}

	public Container run(Container container) {
		if(container.getOutPort() != null && !container.getOutPort().isEmpty()) {
			portPoolService.allocatePort(container.getServer(), Integer.parseInt(container.getOutPort()));
		}
		
		String id = dockerService.runContainer(container.getServer(), this.generateImageName(container.getNamespace(), container.getImageName()), Boolean.FALSE, container.getInPort(), container.getOutPort());
		container.setId(id);
		container.setStatus(DockerService.RUNNING_STATE);
		
		registryService.register(container);
		
		return container;
	}

	public Container run(String server, String namespace, String imageName) {
		Container container = new Container();
		container.setServer(server);
		container.setNamespace(namespace);
		container.setImageName(imageName);
		container.setInPort(DEFAULT_APP_PORT.toString());
		container.setOutPort(portPoolService.allocatePort(server).toString());
		
		String id = dockerService.runContainer(container.getServer(), this.generateImageName(container.getNamespace(), container.getImageName()), Boolean.TRUE, container.getInPort(), container.getOutPort());
		container.setId(id);
		container.setStatus(DockerService.RUNNING_STATE);
		
		registryService.register(container);
		
		return container;
	}
	
	public Container run(String server, String namespace, String imageName, String containerId, String inPort, String outPort) {
		Container container = new Container();
		container.setServer(server);
		container.setNamespace(namespace);
		container.setImageName(imageName);
		container.setInPort(inPort);
		container.setOutPort(outPort);
		
		if(container.getOutPort() != null && !container.getOutPort().isEmpty()) {
			portPoolService.allocatePort(server, Integer.parseInt(container.getOutPort()));
		}
		
		String id = dockerService.runContainer(container.getServer(), this.generateImageName(container.getNamespace(), container.getImageName()), Boolean.FALSE, container.getInPort(), container.getOutPort());
		container.setId(id);
		container.setStatus(DockerService.RUNNING_STATE);
		
		registryService.register(container);
		
		return container;
	}

	private String generateImageName(String namespace, String imageName) {
		return namespace != null? namespace + "/" +imageName : imageName;
	}

	public List<Container> getRunningContainers(String server) {
		return DataTranslator.convertDockerContainerToGenericContainer(dockerService.getRunningContainers(server), server);
	}

}
