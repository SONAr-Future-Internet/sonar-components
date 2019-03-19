package br.ufu.facom.mehar.sonar.cim.model;

public class Container {
	private String namespace = "";
	private String imageName;
	private String id;

	private String inPort;
	private String outPort;

	private String status;
	
	private String server;

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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}
}
