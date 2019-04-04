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
		synchronized (registry) {
			registry.registerContainer(container);
		}
	}

	public void unregister(Container container) {
		synchronized (registry) {
			registry.removeContainer(container);
		}
	}

	public Registry get() {
		return registry;
	}
}
