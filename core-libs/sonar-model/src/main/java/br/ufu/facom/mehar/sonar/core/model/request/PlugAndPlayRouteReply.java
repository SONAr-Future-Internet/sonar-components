package br.ufu.facom.mehar.sonar.core.model.request;

public class PlugAndPlayRouteReply {
	private String assignedIP;
	private String macAddress;
	
	public String getAssignedIP() {
		return assignedIP;
	}
	public void setAssignedIP(String assignedIP) {
		this.assignedIP = assignedIP;
	}
	public String getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
}
