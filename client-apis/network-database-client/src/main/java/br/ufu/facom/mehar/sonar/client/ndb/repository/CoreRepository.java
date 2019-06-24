package br.ufu.facom.mehar.sonar.client.ndb.repository;

import java.util.List;
import java.util.UUID;

import br.ufu.facom.mehar.sonar.core.model.core.Controller;

public interface CoreRepository {
	
	public List<Controller> getControllers();
	public Controller save(Controller controller);
	public Controller getControllerById(UUID idController);
	public List<Controller> getControllersByInterceptor(String interceptor);
}
