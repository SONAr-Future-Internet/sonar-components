package br.ufu.facom.mehar.sonar.core.model.container;

import java.util.ArrayList;
import java.util.List;

public class Registry {
	private List<Server> servers;
	
	public Registry() {
		this.servers = new ArrayList<Server>();
	}

	public List<Server> getServers() {
		return servers;
	}

	public void setServers(List<Server> servers) {
		this.servers = servers;
	}

	public Server getServer(String serverId) {
		for (Server server : this.servers) {
			if (server.getId().equals(serverId)) {
				return server;
			}
		}
		return null;
	}
	
	public Server addServer(String serverId) {
		Server server = new Server();
		server.setId(serverId);
		this.addServer(server);
		return server;
	}
	
	public void addServer(Server server) {
		this.servers.add(server);
	}
	
	public void delServer(String serverId) {
		Server server = getServer(serverId);
		if(server != null) {
			this.delServer(server);
		}
	}
	
	public void delServer(Server server) {
		this.servers.remove(server);
	}

	public static class Server {
		private String id;
		private List<Namespace> namespaces;
		
		public Server() {
			this.namespaces = new ArrayList<Namespace>();
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public List<Namespace> getNamespaces() {
			return namespaces;
		}

		public void setNamespaces(List<Namespace> namespaces) {
			this.namespaces = namespaces;
		}
		
		public Namespace getNamespace(String namespaceId) {
			for (Namespace namespace : this.namespaces) {
				if (namespace.getId().equals(namespaceId)) {
					return namespace;
				}
			}
			return null;
		}
		
		public Namespace addNamespace(String namespaceId) {
			Namespace namespace = new Namespace();
			namespace.setId(namespaceId);
			this.addNamespace(namespace);
			return namespace;
		}
		
		public void addNamespace(Namespace namespace) {
			this.namespaces.add(namespace);
		}
		
		public void delNamespace(String namespaceId) {
			Namespace namespace = getNamespace(namespaceId);
			if(namespace != null) {
				this.delNamespace(namespace);
			}
		}
		
		public void delNamespace(Namespace namespace) {
			this.namespaces.remove(namespace);
		}
	}

	public static class Namespace {
		private String id;
		private List<Image> images;
		
		public Namespace() {
			this.images = new ArrayList<Image>();
		}

		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public List<Image> getImages() {
			return images;
		}

		public void setImages(List<Image> images) {
			this.images = images;
		}
		
		public Image getImage(String imageId) {
			for (Image image : this.images) {
				if (image.getId().equals(imageId)) {
					return image;
				}
			}
			return null;
		}
		
		public Image addImage(String imageId) {
			Image image = new Image();
			image.setId(imageId);
			this.addImage(image);
			return image;
		}
		
		public void addImage(Image image) {
			this.images.add(image);
		}
		
		public void delImage(String imageId) {
			Image image = getImage(imageId);
			if(image != null) {
				this.delImage(image);
			}
		}
		
		public void delImage(Image image) {
			this.images.remove(image);
		}
	}
	
	public static class Image {
		private String id;
		private List<Container> containers;
		
		public Image() {
			this.containers = new ArrayList<Container>();
		}
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public List<Container> getContainers() {
			return containers;
		}
		public void setContainers(List<Container> containers) {
			this.containers = containers;
		}
		
		public Container getContainerById(String containerId) {
			for (Container container : this.containers) {
				if ( (container.getId() == null || container.getId().isEmpty()) && (containerId == null || containerId.isEmpty())) {
					return container;
				}else {
					if(container.getId().equals(containerId)) {
						return container;
					}
				}
			}
			return null;
		}
		
		
		public Container addContainer(String containerId,  String status, String inPort, String outPort) {
			Container container = new Container();
			container.setId(containerId);
			container.setStatus(status);
			container.setInPort(inPort);
			container.setOutPort(outPort);
			this.addContainer(container);
			return container;
		}
		
		public void addContainer(Container container) {
			this.containers.add(container);
		}
		
		public void delContainerById(String containerId) {
			Container container = getContainerById(containerId);
			if(container != null) {
				this.delContainer(container);
			}
		}
		
		public void delContainer(Container container) {
			this.containers.remove(container);
		}

		public Container getContainerByOutPort(String outPort) {
			for (Container container : this.containers) {
				if (container.getOutPort().equals(outPort)) {
					return container;
				}
			}
			return null;
		}
	}

	public static class Container {
		public String inPort;
		public String outPort;
		public String id;
		public String status;
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
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
	}
}
