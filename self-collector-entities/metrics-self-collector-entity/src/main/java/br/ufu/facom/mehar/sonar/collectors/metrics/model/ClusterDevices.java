package br.ufu.facom.mehar.sonar.collectors.metrics.model;

import java.io.Serializable;
import java.util.List;

public class ClusterDevices implements Serializable{

	private static final long serialVersionUID = 1L;
	private List<String> devices;

	public List<String> getDevices() {
		return devices;
	}

	public void setDevices(List<String> devices) {
		this.devices = devices;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{devices: ");
		sb.append(devices);
		sb.append("}");
		return sb.toString();
	}
	
}
