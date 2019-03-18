package br.ufu.facom.mehar.sonar.cim.model;

public class Container {
	private String namespace = "";
	private String imageName;
	private String identification;

	private String inPort;
	private String outPort;

	private String status;

	public String getInPort() {
		return inPort;
	}

	public void setInPort(String inPort) {
		this.inPort = inPort;
	}

	public String getOutPort() {
		return outPort;
	}

	public void setOutPort(String outPort) {
		this.outPort = outPort;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIdentification() {
		return identification;
	}

	public void setIdentification(String identification) {
		this.identification = identification;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

}
