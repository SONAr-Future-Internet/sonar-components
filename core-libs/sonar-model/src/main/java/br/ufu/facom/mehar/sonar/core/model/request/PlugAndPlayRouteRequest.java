package br.ufu.facom.mehar.sonar.core.model.request;

import br.ufu.facom.mehar.sonar.core.model.topology.Element;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;

public class PlugAndPlayRouteRequest {
	private Element attachmentElement;
	private Port attachmentPort;
	private String assignedIP;
	private String macAddress;
	
	public Element getAttachmentElement() {
		return attachmentElement;
	}
	public void setAttachmentElement(Element attachmentElement) {
		this.attachmentElement = attachmentElement;
	}
	public Port getAttachmentPort() {
		return attachmentPort;
	}
	public void setAttachmentPort(Port attachmentPort) {
		this.attachmentPort = attachmentPort;
	}
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
