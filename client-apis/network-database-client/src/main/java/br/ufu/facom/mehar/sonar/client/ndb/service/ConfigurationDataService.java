package br.ufu.facom.mehar.sonar.client.ndb.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.client.ndb.repository.ConfigurationRepository;
import br.ufu.facom.mehar.sonar.core.model.configuration.Configuration;

@Service
public class ConfigurationDataService{
	@Autowired
	private ConfigurationRepository configurationRepository;
	
	public List<Configuration> getControlConfiguration() {
		return configurationRepository.getControlConfiguration();
	}

	
	public List<Configuration> getControlConfigurationByIdElement(UUID idElement) {
		return configurationRepository.getControlConfigurationByIdElement(idElement);
	}

	
	public List<Configuration> saveOrReplaceControlConfiguration(List<Configuration> configurationList) {
		return configurationRepository.saveOrReplaceControlConfiguration(configurationList);
	}

	
	public List<Configuration> saveOrReplaceControlConfigurationByIdElement(UUID idElement, List<Configuration> configurationList) {
		return configurationRepository.saveOrReplaceControlConfigurationByIdElement(idElement, configurationList);
	}

	
	public Configuration addControlConfiguration(Configuration configuration) {
		return configurationRepository.addControlConfiguration(configuration);
	}

	
	public List<Configuration> addControlConfiguration(List<Configuration> configurationList) {
		return configurationRepository.addControlConfiguration(configurationList);
	}

}
