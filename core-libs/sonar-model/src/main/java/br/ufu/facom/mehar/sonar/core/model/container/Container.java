package br.ufu.facom.mehar.sonar.core.model.container;

import java.util.Map;

public class Container {
	private String namespace = "";
	private String imageName;

	private String id;
	private String name;

	private Map<String, String> portMapping;

	private String status;

	private String server;

	private Boolean autoDestroy;
	private Boolean singleton;
	
	public Boolean getSingleton() {
		return singleton;
	}

	public void setSingleton(Boolean singleton) {
		this.singleton = singleton;
	}

	public Boolean getAutoDestroy() {
		return autoDestroy;
	}

	public void setAutoDestroy(Boolean autoDestroy) {
		this.autoDestroy = autoDestroy;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getPortMapping() {
		return portMapping;
	}

	public void setPortMapping(Map<String, String> portMapping) {
		this.portMapping = portMapping;
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
