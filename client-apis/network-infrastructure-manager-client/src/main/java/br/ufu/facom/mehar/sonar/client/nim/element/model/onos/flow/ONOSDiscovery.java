package br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow;

import java.util.List;

public class ONOSDiscovery {
	private List<ONOSDevice> devices;

	public List<ONOSDevice> getDevices() {
		return devices;
	}

	public void setDevices(List<ONOSDevice> devices) {
		this.devices = devices;
	}
}
