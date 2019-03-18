package br.ufu.facom.mehar.sonar.cim.service;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

import br.ufu.facom.mehar.sonar.cim.exception.ContainerCreationException;
import br.ufu.facom.mehar.sonar.cim.exception.ContainerizedInfrastructureManagerException;

public class DockerService {
	private DockerClient docker;
	
	private Logger logger;
	
	public DockerService() {
		try {
			docker = DefaultDockerClient.fromEnv().build();
		}catch(Exception e) {
			logger.error("Error getting DefaultDockerClient from environmetal variables.",e);
		}
	}
	
	public String runContainer(String image, String inPort, String outPort) {
		String[] exposedPorts = null;
		Map<String, List<PortBinding>> portBindings = null;
		
		if(inPort != null && outPort != null && !inPort.isEmpty() && !outPort.isEmpty()) {
			exposedPorts = Arrays.asList(inPort).toArray(new String[1]);

			portBindings = new HashMap<>();
			portBindings.put(inPort, Arrays.asList(PortBinding.of("0.0.0.0", outPort)));
		}
		
		System.out.println(portBindings);
		
		HostConfig hostConfig = HostConfig.builder().autoRemove(true).portBindings(portBindings).build();

		ContainerConfig containerConfig = ContainerConfig.builder()
		    .hostConfig(hostConfig)
		    .image(image)
		    .exposedPorts(exposedPorts)
		    .build();
		
		try {
			
			ContainerCreation creation = docker.createContainer(containerConfig);
			
			String containerId = creation.id();
			
			docker.startContainer(containerId);
			
			return containerId;
			
		} catch (Exception e) {
			throw new ContainerCreationException("Error creating Docker container.",e);
		} 
	}
	
	public List<Container> buscarContainer(String image) {
		try {
			return docker.listContainers(ListContainersParam.filter("image", image));
		} catch (DockerException | InterruptedException e) {
			new ContainerizedInfrastructureManagerException("Error creating Docker container.",e); 
		}
		return new ArrayList<>();
	}
	
	public String createContainer(String image, String inPort, String outPort) {
		String[] exposedPorts = null;
		Map<String, List<PortBinding>> portBindings = null;
		
		if(inPort != null && outPort != null && !inPort.isEmpty() && !outPort.isEmpty()) {
			exposedPorts = Arrays.asList(inPort).toArray(new String[1]);

			portBindings = new HashMap<>();
			portBindings.put(inPort, Arrays.asList(PortBinding.of("0.0.0.0", outPort)));
		}
		
		System.out.println(portBindings);
		
		HostConfig hostConfig = HostConfig.builder().autoRemove(true).portBindings(portBindings).build();

		ContainerConfig containerConfig = ContainerConfig.builder()
		    .hostConfig(hostConfig)
		    .image(image)
		    .exposedPorts(exposedPorts)
		    .build();
		
		try {
			
			ContainerCreation creation = docker.createContainer(containerConfig);
			
			String containerId = creation.id();
			
			return containerId;
			
		} catch (DockerException | InterruptedException e) {
			throw new ContainerCreationException("Error creating Docker container.",e);
		} 
	}
	
	public static void main(String[] args) {
		DockerService dockerService = new DockerService();
		String id = dockerService.runContainer("meharsonar/sonar-dashboard","8080","8181");
		System.out.println(id);
	}
}
