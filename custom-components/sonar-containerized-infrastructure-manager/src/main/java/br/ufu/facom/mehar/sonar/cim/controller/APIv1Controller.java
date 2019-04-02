package br.ufu.facom.mehar.sonar.cim.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.ufu.facom.mehar.sonar.cim.service.ContainerService;
import br.ufu.facom.mehar.sonar.cim.service.RegistryService;
import br.ufu.facom.mehar.sonar.core.model.container.Container;
import br.ufu.facom.mehar.sonar.core.model.container.Registry;

@RestController
@RequestMapping("/api/v1")
public class APIv1Controller {

	@Autowired
	private ContainerService containerService;
	
	@Autowired
	private RegistryService registryService;

	@RequestMapping(value = "/registry", method = RequestMethod.GET)
	public Registry get() {
		return this.registryService.get();
	}
	
	//GET's (specific server)
	@RequestMapping(value = "/server/{server}", method = RequestMethod.GET)
	public List<Container> getContainers(@PathVariable(value="server") String server) {
		return this.containerService.getContainersByServer(server);
	}
	@RequestMapping(value = "/server/{server}/namespace/{namespace}", method = RequestMethod.GET)
	public List<Container> getContainerByNamespace(@PathVariable(value="server") String server, @PathVariable(value="namespace") String namespace) {
		return this.containerService.getContainersByServerAndNamespace(server, namespace);
	}

	@RequestMapping(value = "/server/{server}/namespace/{namespace}/image/{image}", method = RequestMethod.GET)
	public List<Container> getContainerByImageName(@PathVariable(value="server") String server, @PathVariable(value="namespace") String namespace, @PathVariable(value="image") String image) {
		return this.containerService.getContainersByServerNamespaceAndImage(server, namespace, image);
	}
	
	@RequestMapping(value = "/server/{server}/container/{idOrName}", method = RequestMethod.GET)
	public Container getContainer(@PathVariable(value="server") String server, @PathVariable(value = "idOrName") String idOrName) {
		return this.containerService.getContainerByServerAndContainerIdOrName(server, idOrName);
	}
	

	//GET's (all servers)
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public List<Container> getContainersInAllServers() {
		return this.containerService.getContainers();
	}
	@RequestMapping(value = "/namespace/{namespace}", method = RequestMethod.GET)
	public List<Container> getContainerByNamespaceInAllServers( @PathVariable(value="namespace") String namespace) {
		return this.containerService.getContainersByNamespace(namespace);
	}
	@RequestMapping(value = "/namespace/{namespace}/image/{image}", method = RequestMethod.GET)
	public List<Container> getContainerByImageNameInAllServers(@PathVariable(value="namespace") String namespace, @PathVariable(value="image") String image) {
		return this.containerService.getContainersByNamespaceAndImage(namespace, image);
	}
	
	@RequestMapping(value = "/container/{idOrName}", method = RequestMethod.GET)
	public Container getContainerInAllServers(@PathVariable(value = "idOrName") String idOrName) {
		return this.containerService.getContainerByContainerIdOrName(idOrName);
	}
	
	//Register Server
	@RequestMapping(value = "/server/{server}", method = RequestMethod.POST)
	public Boolean registerServer(@PathVariable(value="server") String server) {
		return this.containerService.registerServer(server);
	}
	
	//Create and/or Run
	@RequestMapping(value = "/server/{server}/container", method = RequestMethod.POST)
	public Container runContainer(@RequestBody Container container) {
		return this.containerService.run(container);
	}
	@RequestMapping(value = "/server/{server}/namespace/{namespace}/image/{image}", method = RequestMethod.POST)
	public Container runContainer(@PathVariable(value="server") String server, @PathVariable(value="namespace") String namespace, @PathVariable(value="image") String image) {
		return this.containerService.run(server, namespace, image);
	}
	
	//Run
	@RequestMapping(value = "/server/{server}/namespace/{namespace}/image/{image}/container/{idOrName}", method = RequestMethod.POST)
	public Container runContainer(@PathVariable(value="server") String server, @PathVariable(value="namespace") String namespace, @PathVariable(value="image") String image, @PathVariable(value="idOrName") String idOrName) {
		return this.containerService.run(server, namespace, image, idOrName);
	}
	
	//Stop and Destroy if (autoDestroy)
	@RequestMapping(value = "/server/{server}/namespace/{namespace}/image/{image}/container/{idOrName}", method = RequestMethod.DELETE)
	public Container stopContainer(@PathVariable(value="server") String server, @PathVariable(value="namespace") String namespace, @PathVariable(value="image") String image, @PathVariable(value="idOrName") String idOrName) {
		return this.containerService.stop(server, namespace, image, idOrName);
	}
	@RequestMapping(value = "/server/{server}/namespace/{namespace}/image/{image}", method = RequestMethod.DELETE)
	public List<Container> stopContainer(@PathVariable(value="server") String server, @PathVariable(value="namespace") String namespace, @PathVariable(value="image") String image) {
		return this.containerService.stop(server, namespace, image);
	}
	@RequestMapping(value = "/server/{server}/container", method = RequestMethod.DELETE)
	public Container stopContainer(@RequestBody Container container) {
		return this.containerService.stop(container);
	}
}
