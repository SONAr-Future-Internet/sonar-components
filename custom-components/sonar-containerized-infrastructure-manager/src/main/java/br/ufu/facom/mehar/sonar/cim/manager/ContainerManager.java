package br.ufu.facom.mehar.sonar.cim.manager;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.spotify.docker.client.messages.Container;

public interface ContainerManager {

	Container runContainerById(String server, String containerId);

	Container runContainer(String server, String fullImageName, String containerName, Map<String, String> portMapping,
			Set<String> exposedPorts, List<String> env, Set<String> volumes, List<String> entrypoint, List<String> cmd,
			Boolean autoDestroy, String network);

	void stopContainer(String server, String containerId, Boolean autoDestroy);

	List<Container> getContainersByImage(String server, String fullImageName);

	List<Container> getContainersByNamespace(String server, String namespace);

	Container getContainerById(String server, String id);

	Container getContainerByName(String server, String name);

	List<Container> getContainers(String server);

	List<Container> getRunningContainers(String server);

	void deleteContainer(String server, String containerId);

}
