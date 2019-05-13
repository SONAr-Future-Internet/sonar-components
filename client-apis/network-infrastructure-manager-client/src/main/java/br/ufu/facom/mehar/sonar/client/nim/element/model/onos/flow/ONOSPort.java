package br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow;

import java.util.Map;

public class ONOSPort {
	private String element;		//"of:00000a4fa7c2d743"
	private String port;		//"local"
	private Boolean isEnabled;	//true
	private String type;		//"copper"
	private Integer portSpeed;	//0
	
	private Map<String, String> annotations;	
	//	adminState:	"enabled"
	//	portMac:	"0a:4f:a7:c2:d7:43"
	//	portName:	"br0"

	public String getElement() {
		return element;
	}

	public void setElement(String element) {
		this.element = element;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public Boolean getIsEnabled() {
		return isEnabled;
	}

	public void setIsEnabled(Boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getPortSpeed() {
		return portSpeed;
	}

	public void setPortSpeed(Integer portSpeed) {
		this.portSpeed = portSpeed;
	}

	public Map<String, String> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(Map<String, String> annotations) {
		this.annotations = annotations;
	}
}
