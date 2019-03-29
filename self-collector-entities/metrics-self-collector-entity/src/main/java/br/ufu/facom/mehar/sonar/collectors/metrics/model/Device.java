package br.ufu.facom.mehar.sonar.collectors.metrics.model;

import java.io.Serializable;
import java.util.List;

public class Device implements Serializable {

	private static final long serialVersionUID = 1L;
	private String device;
	private List<Port> ports;
	
	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public List<Port> getPorts() {
		return ports;
	}

	public void setPorts(List<Port> ports) {
		this.ports = ports;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{device: ");
		sb.append(device);
		sb.append(", ports: ");
		sb.append(ports);
		sb.append("}");
		return sb.toString();
	}

}
