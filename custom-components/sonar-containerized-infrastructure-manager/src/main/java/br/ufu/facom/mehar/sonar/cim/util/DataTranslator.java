package br.ufu.facom.mehar.sonar.cim.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.messages.Container.PortMapping;

import br.ufu.facom.mehar.sonar.core.model.container.Container;
import br.ufu.facom.mehar.sonar.core.model.container.ContainerStatus;

public class DataTranslator {
	public static Container translate(com.spotify.docker.client.messages.Container dockerContainer, String server){
		if(dockerContainer == null) {
			return null;
		}
		
		Container container = new Container();
		
		container.setId(dockerContainer.id());
		
		if(dockerContainer.names() != null && !dockerContainer.names().isEmpty()) {
			container.setName(dockerContainer.names().get(0));
		}
		
		container.setServer(server);
		
		String image = dockerContainer.image();
		if(image.contains("/")) {
			String imageParts[] = image.split("/",2);
			container.setNamespace(imageParts[0]);
			if(imageParts[1].contains(":")) {
				String imageParts2[] = imageParts[1].split(":",2);
				container.setImage(imageParts2[0]);
				container.setVersion(imageParts2[1]);
			}else {
				container.setImage(imageParts[1]);
			}
		}else {
			container.setImage(image);
		}
		
		container.setStatus(ContainerStatus.getByDockerStatus(dockerContainer.state()));
		
		ImmutableList<PortMapping> portMapping =  dockerContainer.ports();
		if(portMapping != null && !portMapping.isEmpty()) {
			container.setPortMapping(new HashMap<String, String>());
			for(PortMapping map : portMapping) {
				container.getPortMapping().put(map.privatePort().toString(), map.publicPort().toString());
			}
		}
		
		return container;
	}
	
	public static List<Container> translate(List<com.spotify.docker.client.messages.Container> dockerContainerList, String server){
		if(dockerContainerList == null) {
			return null;
		}
		List<Container> result = new ArrayList<Container>();
		for(com.spotify.docker.client.messages.Container dockerContainer : dockerContainerList) {
			result.add(DataTranslator.translate(dockerContainer, server));
		}
		return result;
	}
}
