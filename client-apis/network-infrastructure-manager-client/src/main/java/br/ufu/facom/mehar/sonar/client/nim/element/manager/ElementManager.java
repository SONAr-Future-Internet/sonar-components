package br.ufu.facom.mehar.sonar.client.nim.element.manager;

import br.ufu.facom.mehar.sonar.core.model.topology.Element;

public interface ElementManager {

	Element discover(String ip);

}
