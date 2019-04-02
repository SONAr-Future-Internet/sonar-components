package br.ufu.facom.mehar.sonar.core.model.container;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Container {
	private String server;

	private String namespace = "";
	private String image;
	private String version;

	private String id;
	private String name;

	private Map<String, String> portMapping;
	private List<String> env;
	private Set<String> exposedPorts;
	private List<String> cmd;
	private List<String> entrypoint;
	private Set<String> volumes;
	
	private String status;

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

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public List<String> getEnv() {
		return env;
	}

	public void setEnv(List<String> env) {
		this.env = env;
	}

	public Set<String> getExposedPorts() {
		return exposedPorts;
	}

	public void setExposedPorts(Set<String> exposedPorts) {
		this.exposedPorts = exposedPorts;
	}

	public List<String> getCmd() {
		return cmd;
	}

	public void setCmd(List<String> cmd) {
		this.cmd = cmd;
	}

	public List<String> getEntrypoint() {
		return entrypoint;
	}

	public void setEntrypoint(List<String> entrypoint) {
		this.entrypoint = entrypoint;
	}

	public Set<String> getVolumes() {
		return volumes;
	}

	public void setVolumes(Set<String> volumes) {
		this.volumes = volumes;
	}

	@JsonIgnore
	public String getImageNameWithNamespace() {
		return Container.getImageNameWithNamespace(namespace, image);
	}
	
	@JsonIgnore
	public String getImageNameWithNamespaceAndVersion() {
		return Container.getImageNameWithNamespaceAndVersion(namespace, image, version);
	}
	
	@JsonIgnore
	public String getImageNameWithVersion() {
		return Container.getImageNameWithVersion(image, version);
	}
	
	public static String getImageNameWithNamespaceAndVersion(String namespace, String image, String version) {
		return Container.getImageNameWithNamespace(namespace, Container.getImageNameWithVersion(image, version));
	}
	
	public static String getImageNameWithNamespace(String namespace, String image) {
		return  namespace != null? namespace + "/" +image : image;
	}
	
	private static String getImageNameWithVersion(String image, String version) {
		return version != null? image + ":" +version : image;
	}
	

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Container other = (Container) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (image == null) {
			if (other.image != null)
				return false;
		} else if (!image.equals(other.image))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (namespace == null) {
			if (other.namespace != null)
				return false;
		} else if (!namespace.equals(other.namespace))
			return false;
		if (portMapping == null) {
			if (other.portMapping != null)
				return false;
		} else if (!portMapping.equals(other.portMapping))
			return false;
		if (server == null) {
			if (other.server != null)
				return false;
		} else if (!server.equals(other.server))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
	
	
}
