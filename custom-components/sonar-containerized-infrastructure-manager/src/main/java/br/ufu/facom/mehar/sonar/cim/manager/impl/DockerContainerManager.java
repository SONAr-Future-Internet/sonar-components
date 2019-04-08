package br.ufu.facom.mehar.sonar.cim.manager.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

import br.ufu.facom.mehar.sonar.cim.exception.ContainerInstantiationException;
import br.ufu.facom.mehar.sonar.cim.exception.ContainerSearchException;
import br.ufu.facom.mehar.sonar.cim.exception.ContainerServerConnectionException;
import br.ufu.facom.mehar.sonar.cim.exception.UnsupportedMethodException;
import br.ufu.facom.mehar.sonar.cim.manager.ContainerManager;

@Component("docker")
public class DockerContainerManager implements ContainerManager {

	private static final int MAX_STOP_TIME = 5;

	/*
	 * Connect and Disconnect Methods
	 */
	private DockerClient connectToDockerServer(String server) {
		try {
			if (server != null && (server.equalsIgnoreCase("local") || server.equalsIgnoreCase("localhost"))) {
				return DefaultDockerClient.fromEnv().build();
			} else {
				throw new UnsupportedMethodException("The requested method is not supported yet.");
			}
		} catch (DockerCertificateException e) {
			throw new ContainerServerConnectionException("Can't open connection to server " + server + ".", e);
		}
	}

	private void closeConnection(DockerClient client) {

		if (client != null) {

			client.close();
		}
	}

	/*
	 * Run Container
	 */
	@Override
	public Container runContainerById(String server, String containerId) {
		DockerClient docker = null;
		try {
			docker = this.connectToDockerServer(server);

			// Running
			docker.startContainer(containerId);

			// Querying
			Container container = getContainerById(containerId, docker);
			if (container != null) {
				return container;
			} else {
				throw new ContainerInstantiationException(
						"Error recovering container with id " + containerId + " in server " + server + ".");
			}

		} catch (DockerException | InterruptedException e) {
			throw new ContainerInstantiationException(
					"Error running with id " + containerId + " in server " + server + ".", e);
		} finally {
			this.closeConnection(docker);
		}
	}

	@Override
	public Container runContainer(String server, String fullImageName, String containerName,
			Map<String, String> portMapping, Set<String> exposedPorts, List<String> env, Set<String> volumes,
			List<String> entrypoint, List<String> cmd, Boolean autoDestroy, String network) {

		HostConfig.Builder hostConfigBuilder = HostConfig.builder().autoRemove(autoDestroy);
		if (portMapping != null && !portMapping.isEmpty()) {
			Map<String, List<PortBinding>> portBindings = new HashMap<String, List<PortBinding>>();
			for (String inPort : portMapping.keySet()) {
				String outPort = portMapping.get(inPort);
				portBindings.put(inPort, Arrays.asList(PortBinding.of("0.0.0.0", outPort)));
			}

			if (exposedPorts == null) {
				exposedPorts = new HashSet<String>();
			}

			exposedPorts.addAll(portMapping.keySet());

			hostConfigBuilder = hostConfigBuilder.portBindings(portBindings);
		}

		if (network != null && !network.isEmpty()) {
			hostConfigBuilder.networkMode(network);
		}

		HostConfig hostConfig = hostConfigBuilder.build();

		ContainerConfig.Builder containerConfigBuilder = ContainerConfig.builder().hostConfig(hostConfig)
				.image(fullImageName);
		if (exposedPorts != null && !exposedPorts.isEmpty()) {
			containerConfigBuilder = containerConfigBuilder.exposedPorts(exposedPorts);
		}
		if (env != null && !env.isEmpty()) {
			containerConfigBuilder.env(env);
		}
		if (volumes != null && !volumes.isEmpty()) {
			containerConfigBuilder.volumes(volumes);
		}
		if (entrypoint != null && !entrypoint.isEmpty()) {
			containerConfigBuilder.entrypoint(entrypoint);
		}
		if (cmd != null && !cmd.isEmpty()) {
			containerConfigBuilder.cmd(cmd);
		}

		ContainerConfig containerConfig = containerConfigBuilder.build();

		DockerClient docker = null;
		try {
			docker = this.connectToDockerServer(server);

			// Creation
			ContainerCreation creation = docker.createContainer(containerConfig, containerName);
			String containerId = creation.id();

			// Running
			docker.startContainer(containerId);

			// Querying
			Container container = getContainerById(containerId, docker);
			if (container != null) {
				return container;
			} else {
				throw new ContainerInstantiationException(
						"Error recovering container with id " + containerId + " in server " + server + ".");
			}
		} catch (DockerException | InterruptedException e) {
			throw new ContainerInstantiationException(
					"Error creating container from image " + fullImageName + " in server " + server + ".", e);
		} finally {
			this.closeConnection(docker);
		}
	}

	/*
	 * Stop Container
	 */
	@Override
	public void stopContainer(String server, String containerId, Boolean autoDestroy) {
		DockerClient docker = null;
		try {
			docker = this.connectToDockerServer(server);

			// Running
			docker.stopContainer(containerId, MAX_STOP_TIME);
			
			if (autoDestroy) {
				deleteContainer(containerId, docker);
			}

		} catch (DockerException | InterruptedException e) {
			throw new ContainerInstantiationException(
					"Error running with id " + containerId + " in server " + server + ".", e);
		} finally {
			this.closeConnection(docker);
		}
	}

	@Override
	public void deleteContainer(String server, String containerId) {
		DockerClient docker = null;
		try {
			docker = this.connectToDockerServer(server);
			
			// Querying
			Container container = getContainerById(containerId, docker);
			if (container != null) {
				docker.removeContainer(containerId);
			}

		} catch (DockerException | InterruptedException e) {
			throw new ContainerInstantiationException(
					"Error running with id " + containerId + " in server " + server + ".", e);
		} finally {
			this.closeConnection(docker);
		}
	}

	private void deleteContainer(String containerId, DockerClient docker)
			throws DockerException, InterruptedException {
		// Querying
		Container container = getContainerById(containerId, docker);
		if (container != null) {
			docker.removeContainer(containerId);
		}
	}

	/*
	 * Get Containers
	 */
	@Override
	public List<Container> getContainersByImage(String server, String fullImageName) {
		List<Container> result = new ArrayList<Container>();
		DockerClient docker = null;

		String imageName = fullImageName.contains(":") ? fullImageName.split(":", 2)[0] : fullImageName;
		try {
			docker = this.connectToDockerServer(server);
			List<Container> containers = docker.listContainers(ListContainersParam.allContainers());
			if (containers != null && !containers.isEmpty()) {
				for (int i = 0; i < containers.size(); i++) {
					String fullContainerImageName = containers.get(i).image();
					String containerImageName = fullContainerImageName.contains(":")
							? fullContainerImageName.split(":", 2)[0]
							: fullImageName;
					if (containerImageName.equals(imageName)) {
						result.add(containers.get(i));
					}
				}
			}
		} catch (DockerException | InterruptedException e) {
			new ContainerSearchException("Error searching for Docker container.", e);
		} finally {
			this.closeConnection(docker);
		}
		return result;
	}

	@Override
	public List<Container> getContainersByNamespace(String server, String namespace) {
		List<Container> result = new ArrayList<Container>();
		DockerClient docker = null;
		try {
			docker = this.connectToDockerServer(server);
			List<Container> containers = docker.listContainers(ListContainersParam.allContainers());
			if (containers != null && !containers.isEmpty()) {
				for (int i = 0; i < containers.size(); i++) {
					if (containers.get(i).image().startsWith(namespace + "/")) {
						result.add(containers.get(i));
					}
				}
			}
		} catch (DockerException | InterruptedException e) {
			new ContainerSearchException("Error searching for Docker container.", e);
		} finally {
			this.closeConnection(docker);
		}
		return result;
	}

	@Override
	public Container getContainerById(String server, String id) {
		DockerClient docker = null;
		try {
			docker = this.connectToDockerServer(server);
			return getContainerById(id, docker);
		} catch (DockerException | InterruptedException e) {
			new ContainerSearchException("Error searching for Docker container.", e);
		} finally {
			this.closeConnection(docker);
		}
		return null;
	}

	private Container getContainerById(String containerId, DockerClient docker)
			throws DockerException, InterruptedException {
		// First try
		List<Container> result = docker.listContainers(ListContainersParam.filter("id", containerId));
		if (result != null && !result.isEmpty()) {
			return result.get(0);
		}

		result = docker.listContainers(ListContainersParam.allContainers());
		if (result != null && !result.isEmpty()) {
			for (Container container : result) {
				if (container.id().equalsIgnoreCase(containerId)) {
					return container;
				}
			}
		}
		return null;
	}

	@Override
	public Container getContainerByName(String server, String name) {
		DockerClient docker = null;
		try {
			docker = this.connectToDockerServer(server);
			List<Container> result = docker.listContainers(ListContainersParam.filter("name", name));
			if (result != null && !result.isEmpty()) {
				return result.get(0);
			}
		} catch (DockerException | InterruptedException e) {
			new ContainerSearchException("Error searching for Docker container.", e);
		} finally {
			this.closeConnection(docker);
		}
		return null;
	}

	@Override
	public List<Container> getContainers(String server) {
		DockerClient docker = null;
		try {
			docker = this.connectToDockerServer(server);
			return docker.listContainers(ListContainersParam.allContainers());
		} catch (DockerException | InterruptedException e) {
			new ContainerSearchException("Error searching for Docker container.", e);
		} finally {
			this.closeConnection(docker);
		}
		return new ArrayList<>();
	}

	@Override
	public List<Container> getRunningContainers(String server) {
		DockerClient docker = null;
		try {
			docker = this.connectToDockerServer(server);
			return docker.listContainers(ListContainersParam.withStatusRunning());
		} catch (DockerException | InterruptedException e) {
			new ContainerSearchException("Error searching for Docker container.", e);
		} finally {
			this.closeConnection(docker);
		}
		return new ArrayList<>();
	}
}
