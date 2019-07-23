package br.ufu.facom.mehar.sonar.client.ndb.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.client.ndb.repository.CoreRepository;
import br.ufu.facom.mehar.sonar.core.model.core.Controller;

@Service
public class CoreDataService{

	@Autowired
	private CoreRepository repository;

	public List<Controller> getControllers() {
		return repository.getControllers();
	}

	public Controller save(Controller controller) {
		return repository.save(controller);
	}

	public Controller getControllerById(UUID idController) {
		return repository.getControllerById(idController);
	}

	public List<Controller> getControllersByInterceptor(String interceptor) {
		return repository.getControllersByInterceptor(interceptor);
	}

	public Boolean deleteControllers() {
		return repository.deleteControllers();
	}

}
