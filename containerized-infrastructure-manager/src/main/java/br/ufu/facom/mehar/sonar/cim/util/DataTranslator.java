package br.ufu.facom.mehar.sonar.cim.util;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.messages.Container.PortMapping;

import br.ufu.facom.mehar.sonar.cim.model.Container;

public class DataTranslator {
	public static Container convertDockerContainerToGenericContainer(com.spotify.docker.client.messages.Container dockerContainer, String server){
		Container container = new Container();
		
		container.setId(dockerContainer.id());
		
		container.setServer(server);
		
		String image = dockerContainer.image();
		if(image.contains("/")) {
			String imageParts[] = image.split("/",2);
			container.setNamespace(imageParts[0]);
			container.setImageName(imageParts[1]);
		}else {
			container.setImageName(image);
		}
		
		container.setStatus(dockerContainer.state());
		
		ImmutableList<PortMapping> portMapping =  dockerContainer.ports();
		if(portMapping != null && !portMapping.isEmpty()) {
			PortMapping mapping = portMapping.get(0);
			container.setInPort(mapping.privatePort().toString());
			container.setOutPort(mapping.publicPort().toString());
		}

		return container;
	}
	
	public static List<Container> convertDockerContainerToGenericContainer(List<com.spotify.docker.client.messages.Container> dockerContainerList, String server){
		List<Container> result = new ArrayList<Container>();
		for(com.spotify.docker.client.messages.Container dockerContainer : dockerContainerList) {
			result.add(DataTranslator.convertDockerContainerToGenericContainer(dockerContainer, server));
		}
		return result;
	}
}
