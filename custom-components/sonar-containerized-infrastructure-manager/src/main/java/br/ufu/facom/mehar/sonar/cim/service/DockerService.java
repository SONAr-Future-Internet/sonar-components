package br.ufu.facom.mehar.sonar.cim.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.Container.PortMapping;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

import br.ufu.facom.mehar.sonar.cim.exception.ContainerInstantiationException;
import br.ufu.facom.mehar.sonar.cim.exception.ContainerSearchException;
import br.ufu.facom.mehar.sonar.cim.exception.ContainerServerConnectionException;
import br.ufu.facom.mehar.sonar.cim.exception.UnsupportedMethodException;

@Service
public class DockerService {

	public static final String RUNNING_STATE = "running";
	public static final String STOPPED_STATE = "stopped";
	private static final int MAX_STOP_TIME = 5;

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

	public String runContainer(String server, String image, Boolean singleton, String inPort, String outPort) {

		String[] exposedPorts = null;
		Map<String, List<PortBinding>> portBindings = null;

		if (inPort != null && outPort != null && !inPort.isEmpty() && !outPort.isEmpty()) {
			exposedPorts = Arrays.asList(inPort).toArray(new String[1]);

			portBindings = new HashMap<>();
			portBindings.put(inPort, Arrays.asList(PortBinding.of("0.0.0.0", outPort)));
		}

		HostConfig hostConfig = HostConfig.builder().autoRemove(true).portBindings(portBindings).build();

		ContainerConfig containerConfig = ContainerConfig.builder().hostConfig(hostConfig).image(image)
				.exposedPorts(exposedPorts).build();

		DockerClient docker = null;
		try {
			docker = this.connectToDockerServer(server);

			List<Container> containersAlreadyCreated = docker
					.listContainers(ListContainersParam.create("image", image));
			if (containersAlreadyCreated != null && !containersAlreadyCreated.isEmpty()) {
				for (int i = 0; i < containersAlreadyCreated.size(); i++) {
					if (containersAlreadyCreated.get(i).state().equals(RUNNING_STATE)) {
						if (singleton) {
							throw new ContainerInstantiationException(
									"There is already a container running from image " + image + " in server " + server
											+ " with id " + containersAlreadyCreated.get(i).id() + ".");
						} else {
							if (outPort != null && !outPort.isEmpty()) {
								ImmutableList<PortMapping> portMapping = containersAlreadyCreated.get(i).ports();
								if (portMapping != null && !portMapping.isEmpty()) {
									PortMapping mapping = portMapping.get(0);
									if (outPort.equals(mapping.publicPort().toString())) {
										throw new ContainerInstantiationException(
												"There is already a container running from image " + image
														+ " in server " + server + " with id "
														+ containersAlreadyCreated.get(i).id() + " on exposed port "
														+ outPort + ".");
									}
								}
							}
						}
					} else {
						docker.removeContainer(containersAlreadyCreated.get(i).id());
					}
				}
			}

			ContainerCreation creation = docker.createContainer(containerConfig);
			String containerId = creation.id();
			docker.startContainer(containerId);
			return containerId;

		} catch (DockerException | InterruptedException e) {
			throw new ContainerInstantiationException(
					"Error creating container from image " + image + " in server " + server + ".", e);
		} finally {
			this.closeConnection(docker);
		}
	}

	public List<Container> stopContainer(String server, String image) {
		List<Container> result = new ArrayList<Container>();
		DockerClient docker = null;
		try {
			docker = this.connectToDockerServer(server);
			List<Container> containersAlreadyCreated = docker
					.listContainers(ListContainersParam.create("image", image));
			if (containersAlreadyCreated != null && !containersAlreadyCreated.isEmpty()) {
				for (int i = 0; i < containersAlreadyCreated.size(); i++) {
					Container container = containersAlreadyCreated.get(i);
					if (container.state().equals(RUNNING_STATE)) {
						docker.stopContainer(container.id(), MAX_STOP_TIME);
						result.add(container);
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

	public List<Container> getContainersByImage(String server, String image) {
		DockerClient docker = null;
		try {
			docker = this.connectToDockerServer(server);
			return docker.listContainers(ListContainersParam.create("image", image));
		} catch (DockerException | InterruptedException e) {
			new ContainerSearchException("Error searching for Docker container.", e);
		} finally {
			this.closeConnection(docker);
		}
		return new ArrayList<>();
	}

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


	public Container getContainerById(String server, String id) {
		DockerClient docker = null;
		try {
			docker = this.connectToDockerServer(server);
			List<Container> result = docker.listContainers(ListContainersParam.filter("id", id));
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

	public Container runContainerById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

//	public List<Container> getRunningContainers(String server) {
//		DockerClient docker = null;
//		try {
//			docker = this.connectToDockerServer(server);
//			return docker.listContainers(ListContainersParam.withStatusRunning());
//		} catch (DockerException | InterruptedException e) {
//			new ContainerSearchException("Error searching for Docker container.", e);
//		} finally {
//			this.closeConnection(docker);
//		}
//		return new ArrayList<>();
//	}
	
//	public List<Container> getRunningContainersByNamespace(String server, String namespace) {
//		List<Container> result = new ArrayList<Container>();
//		DockerClient docker = null;
//		try {
//			docker = this.connectToDockerServer(server);
//			List<Container> containers = docker.listContainers(ListContainersParam.withStatusRunning(),
//					ListContainersParam.allContainers());
//			if (containers != null && !containers.isEmpty()) {
//				for (int i = 0; i < containers.size(); i++) {
//					if (containers.get(i).image().startsWith(namespace + "/")) {
//						result.add(containers.get(i));
//					}
//				}
//			}
//		} catch (DockerException | InterruptedException e) {
//			new ContainerSearchException("Error searching for Docker container.", e);
//		} finally {
//			this.closeConnection(docker);
//		}
//		return result;
//	}
	
//	public List<Container> getRunningContainersByImage(String server, String image) {
//		DockerClient docker = null;
//		try {
//			docker = this.connectToDockerServer(server);
//			return docker.listContainers(ListContainersParam.withStatusRunning(),
//					ListContainersParam.create("image", image));
//		} catch (DockerException | InterruptedException e) {
//			new ContainerSearchException("Error searching for Docker container.", e);
//		} finally {
//			this.closeConnection(docker);
//		}
//		return new ArrayList<>();
//	}

}
