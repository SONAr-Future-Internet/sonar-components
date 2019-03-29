package br.ufu.facom.mehar.sonar.core.model.context;

import java.util.List;

import br.ufu.facom.mehar.sonar.core.model.service.Service;

public class Scenario {
	private Moment moment;
	private Snapshot snapshot;
	private List<Service> activeServices;
	public Moment getMoment() {
		return moment;
	}
	public void setMoment(Moment moment) {
		this.moment = moment;
	}
	public Snapshot getSnapshot() {
		return snapshot;
	}
	public void setSnapshot(Snapshot snapshot) {
		this.snapshot = snapshot;
	}
	public List<Service> getActiveServices() {
		return activeServices;
	}
	public void setActiveServices(List<Service> activeServices) {
		this.activeServices = activeServices;
	}
	
	
}
