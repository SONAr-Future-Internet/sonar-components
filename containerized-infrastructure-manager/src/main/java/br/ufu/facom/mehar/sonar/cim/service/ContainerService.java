package br.ufu.facom.mehar.sonar.cim.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import br.ufu.facom.mehar.sonar.cim.model.Container;

public class ContainerService {
	@Autowired
	DockerService dockerService;

	public List<Container> get(String namespace, String imageName) {
		// TODO Auto-generated method stub
		return null;
	}

	public Container get(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public Container stop(String namespace, String imageName) {
		// TODO Auto-generated method stub
		return null;
	}

	public Container run(Container container) {
		String id = dockerService.runContainer(container.getNamespace()+"/"+container.getImageName(), container.getInPort(), container.getOutPort());
		container.setIdentification(id);
		return container;
	}

	public Container run(String namespace, String imageName) {
		Container container = new Container();
		container.setNamespace(namespace);
		container.setImageName(imageName);
		
		return this.run(container);
	}

}
