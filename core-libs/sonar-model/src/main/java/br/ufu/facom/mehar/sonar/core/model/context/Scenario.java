package br.ufu.facom.mehar.sonar.core.model.context;

import java.util.List;

import br.ufu.facom.mehar.sonar.core.model.service.Service;

/*
 * Snapshot of 'Network' using SONAr Services Abstraction
 */
public class Scenario extends Snapshot{
	private List<Service> activeServices;
	
	public List<Service> getActiveServices() {
		return activeServices;
	}
	public void setActiveServices(List<Service> activeServices) {
		this.activeServices = activeServices;
	}
	
	
}
