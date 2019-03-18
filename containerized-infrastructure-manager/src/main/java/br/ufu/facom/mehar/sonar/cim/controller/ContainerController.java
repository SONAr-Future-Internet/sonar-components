package br.ufu.facom.mehar.sonar.cim.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import br.ufu.facom.mehar.sonar.cim.model.Container;
import br.ufu.facom.mehar.sonar.cim.service.ContainerService;

@Controller
@RequestMapping("/api/v1/container")
public class ContainerController {

	@Autowired
	private ContainerService containerService;

	@RequestMapping(value = "/{namespace}", method = RequestMethod.GET)
	public List<Container> getContainerByNamespace(@PathVariable(value="namespace") String namespace) {
		return this.containerService.get(namespace, null);
	}

	@RequestMapping(value = "/{namespace}/{imageName}", method = RequestMethod.GET)
	public List<Container> getContainerByImageName(@PathVariable(value="namespace") String namespace, @PathVariable(value="imageName") String imageName) {
		return this.containerService.get(namespace, imageName);
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public Container get(@RequestParam(value = "id") String id) {
		return this.containerService.get(id);
	}
	
	@RequestMapping(value = "/{namespace}/{imageName}", method = RequestMethod.POST)
	public Container runContainer(@PathVariable(value="namespace") String namespace, @PathVariable(value="imageName") String imageName) {
		return this.containerService.run(namespace, imageName);
	}
	
	@RequestMapping(value = "/{namespace}/{imageName}", method = RequestMethod.DELETE)
	public Container stopContainer(@PathVariable(value="namespace") String namespace, @PathVariable(value="imageName") String imageName) {
		return this.containerService.stop(namespace, imageName);
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public Container runContainerBySpec(@RequestBody Container container) {
		return this.containerService.run(container);
	}
}
