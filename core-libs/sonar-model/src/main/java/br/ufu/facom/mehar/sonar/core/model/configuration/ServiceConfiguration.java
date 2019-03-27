package br.ufu.facom.mehar.sonar.core.model.configuration;

import java.util.List;

import br.ufu.facom.mehar.sonar.core.model.context.Scenario;
import br.ufu.facom.mehar.sonar.core.model.service.Service;

public class ServiceConfiguration {
	// Configuration of a specific service...
	private Service service;
	
	// for a specific scenario...
	private Scenario scenario;
	
	// through the command list bellow
	private List<Configuration> configurationList;

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public Scenario getScenario() {
		return scenario;
	}

	public void setScenario(Scenario scenario) {
		this.scenario = scenario;
	}

	public List<Configuration> getConfigurationList() {
		return configurationList;
	}

	public void setConfigurationList(List<Configuration> configurationList) {
		this.configurationList = configurationList;
	}
}
