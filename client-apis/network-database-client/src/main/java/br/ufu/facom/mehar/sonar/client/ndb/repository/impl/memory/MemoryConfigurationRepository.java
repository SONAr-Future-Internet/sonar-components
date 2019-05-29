package br.ufu.facom.mehar.sonar.client.ndb.repository.impl.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.ufu.facom.mehar.sonar.client.ndb.repository.ConfigurationRepository;
import br.ufu.facom.mehar.sonar.core.model.configuration.Configuration;

@Repository
public class MemoryConfigurationRepository implements ConfigurationRepository{
	private static final List<Configuration> controlConfigurationRepository = new ArrayList<Configuration>();
//	private static final List<ServiceConfiguration> serviceConfigurationRepository = new ArrayList<ServiceConfiguration>();
	
	@Override
	public List<Configuration> getControlConfiguration() {
		return new ArrayList<Configuration>(controlConfigurationRepository);
	}

	@Override
	public List<Configuration> getControlConfigurationByIdElement(UUID idElement) {
		List<Configuration> resultList = new ArrayList<Configuration>();
		for(Configuration configuration : controlConfigurationRepository) {
			if(idElement.equals(configuration.getIdElement())) {
				resultList.add(configuration);
			}
		}
		return resultList;
	}
	
	@Override
	public List<Configuration> saveOrReplaceControlConfiguration(List<Configuration> configurationList) {
		controlConfigurationRepository.clear();
		controlConfigurationRepository.addAll(configurationList);
		return configurationList;
	}
	
	@Override
	public List<Configuration> saveOrReplaceControlConfigurationByIdElement(UUID idElement, List<Configuration> configurationList) {
		controlConfigurationRepository.removeAll(this.getControlConfigurationByIdElement(idElement));
		controlConfigurationRepository.addAll(configurationList);
		return configurationList;
	}
	
	@Override
	public Configuration addControlConfiguration(Configuration configuration) {
		controlConfigurationRepository.add(configuration);
		return configuration;
	}
	
	@Override
	public List<Configuration> addControlConfiguration(List<Configuration> configurationList) {
		controlConfigurationRepository.addAll(configurationList);
		return configurationList;
	}
}
