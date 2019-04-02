package br.ufu.facom.mehar.sonar.cim.service;

import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.core.model.container.Container;
import br.ufu.facom.mehar.sonar.core.model.container.Registry;

@Service
public class RegistryService {
	private static final Registry registry = new Registry();
	
	public void registerServer(String serverIdentification) {
		synchronized (registry) {
			registry.registerServer(serverIdentification);
		}
	}
	
	public void unregisterServer(String serverIdentification) {
		synchronized (registry) {
			registry.removeServer(serverIdentification);
		}
	}

	public void register(Container container) {
//		synchronized (registry) {
//			Registry.Server server = registry.getServer(container.getServer());
//			if (server == null) {
//				server = registry.addServer(container.getServer());
//			}
//
//			Registry.Namespace namespace = server.getNamespace(container.getNamespace());
//			if (namespace == null) {
//				namespace = server.addNamespace(container.getNamespace());
//			}
//
//			Registry.Image image = namespace.getImage(container.getImageName());
//			if (image == null) {
//				image = namespace.addImage(container.getImageName());
//			}
//
//			Registry.Container service = null;
//			if (container.getId() != null && !container.getId().isEmpty()) {
//				service = image.getContainerById(container.getId());
//			} else {
//				service = image.getContainerByOutPort(container.getOutPort());
//			}
//
//			if (service == null) {
//				image.addContainer(container.getId(), container.getStatus(), container.getInPort(),
//						container.getOutPort());
//			}
//		}
	}

	public void unregister(Container container) {
//		synchronized (registry) {
//			Registry.Server server = registry.getServer(container.getServer());
//			if (server != null) {
//
//				Registry.Namespace namespace = server.getNamespace(container.getNamespace());
//				if (namespace != null) {
//				}
//				Registry.Image image = namespace.getImage(container.getImageName());
//				if (image != null) {
//
//					Registry.Container service = null;
//					if (container.getId() != null && !container.getId().isEmpty()) {
//						service = image.getContainerById(container.getId());
//					} else {
//						service = image.getContainerByOutPort(container.getOutPort());
//					}
//
//					if (service != null) {
//						image.delContainer(service);
//
//						if (image.getContainers().isEmpty()) {
//							namespace.delImage(image);
//
//							if (namespace.getImages().isEmpty()) {
//								server.delNamespace(namespace);
//							}
//						}
//					}
//				}
//			}
//		}
	}

	public Registry get() {
		return registry;
	}
}
