package br.ufu.facom.mehar.sonar.cim.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.ufu.facom.mehar.sonar.cim.model.Container;
import br.ufu.facom.mehar.sonar.cim.model.Registry;
import br.ufu.facom.mehar.sonar.cim.service.ContainerService;
import br.ufu.facom.mehar.sonar.cim.service.RegistryService;

@RestController
@RequestMapping("/api/v1")
public class APIv1Controller {

	@Autowired
	private ContainerService containerService;
	
	@Autowired
	private RegistryService registryService;

	@RequestMapping(value = "/server/{server}/namespace/{namespace}", method = RequestMethod.GET)
	public List<Container> getContainerByNamespace(@PathVariable(value="server") String server, @PathVariable(value="namespace") String namespace) {
		return this.containerService.getContainersByServerAndNamespace(server, namespace);
	}

	@RequestMapping(value = "/server/{server}/namespace/{namespace}/image/{image}", method = RequestMethod.GET)
	public List<Container> getContainerByImageName(@PathVariable(value="server") String server, @PathVariable(value="namespace") String namespace, @PathVariable(value="image") String image) {
		return this.containerService.getContainersByServerNamespaceAndImage(server, namespace, image);
	}
	
	
	@RequestMapping(value = "/server/{server}/namespace/{namespace}/image/{image}", method = RequestMethod.POST)
	public Container runContainer(@PathVariable(value="server") String server, @PathVariable(value="namespace") String namespace, @PathVariable(value="image") String image) {
		return this.containerService.run(server, namespace, image);
	}
	
	@RequestMapping(value = "/server/{server}/namespace/{namespace}/image/{image}", method = RequestMethod.DELETE)
	public List<Container> stopContainer(@PathVariable(value="server") String server, @PathVariable(value="namespace") String namespace, @PathVariable(value="image") String image) {
		return this.containerService.stop(server, namespace, image);
	}
	
	@RequestMapping(value = "/server/{server}/container", method = RequestMethod.POST)
	public Container runContainer(@RequestBody Container container) {
		return this.containerService.run(container);
	}
	
	@RequestMapping(value = "/server/{server}/container", method = RequestMethod.GET)
	public Container get(@PathVariable(value="server") String server, @RequestParam(value = "id") String id) {
		return this.containerService.getContainerByServerAndId(server, id);
	}
	
	@RequestMapping(value = "/registry", method = RequestMethod.GET)
	public Registry get() {
		return this.registryService.get();
	}
}
