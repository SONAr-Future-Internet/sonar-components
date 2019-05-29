package br.ufu.facom.mehar.sonar.client.ndb.repository;

import java.util.List;
import java.util.UUID;

import br.ufu.facom.mehar.sonar.core.model.configuration.Configuration;

public interface ConfigurationRepository {
	List<Configuration> getControlConfiguration();
	List<Configuration> getControlConfigurationByIdElement(UUID idElement);
	List<Configuration> saveOrReplaceControlConfiguration(List<Configuration> configurationList);
	List<Configuration> saveOrReplaceControlConfigurationByIdElement(UUID idElement, List<Configuration> configurationList);
	Configuration addControlConfiguration(Configuration configuration);
	List<Configuration> addControlConfiguration(List<Configuration> configurationList);
}
