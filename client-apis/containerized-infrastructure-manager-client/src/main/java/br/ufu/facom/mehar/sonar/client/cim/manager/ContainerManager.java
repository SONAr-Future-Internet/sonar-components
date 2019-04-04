package br.ufu.facom.mehar.sonar.client.cim.manager;

import java.util.List;

import br.ufu.facom.mehar.sonar.core.model.container.Container;

public interface ContainerManager {

	Container run(String managerIp, Container container);

	Container stop(String managerIp, Container container);

	List<Container> get(String managerIp);

}
