package br.ufu.facom.mehar.sonar.core.model.configuration;

import java.util.List;

import br.ufu.facom.mehar.sonar.core.model.context.Snapshot;
import br.ufu.facom.mehar.sonar.core.model.service.Service;

public class ServiceConfiguration {
	// Configuration of a specific service...
	private Service service;
	
	// for a specific 'moment'...
	private Snapshot snapshot;
	
	// through the command list bellow
	private List<Configuration> configurationList;

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public Snapshot getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(Snapshot snapshot) {
		this.snapshot = snapshot;
	}

	public List<Configuration> getConfigurationList() {
		return configurationList;
	}

	public void setConfigurationList(List<Configuration> configurationList) {
		this.configurationList = configurationList;
	}
}
