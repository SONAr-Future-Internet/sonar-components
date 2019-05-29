package br.ufu.facom.mehar.sonar.client.ndb.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.client.ndb.repository.PropertyRepository;
import br.ufu.facom.mehar.sonar.core.model.property.ConfigurationProperty;
import br.ufu.facom.mehar.sonar.core.model.property.DataProperty;

@Service
public class PropertyService {

	@Autowired
	private PropertyRepository propertyRepository;

	public List<ConfigurationProperty> getConfiguration() {
		return propertyRepository.getConfiguration();
	}

	public List<ConfigurationProperty> getConfiguration(String group) {
		return propertyRepository.getConfiguration();
	}

	public ConfigurationProperty getConfiguration(String group, String key) {
		return propertyRepository.getConfiguration(group, key);
	}

	public Boolean setProperty(ConfigurationProperty property) {
		return propertyRepository.setProperty(property);
	}

	public Boolean setProperty(String group, String key, String value) {
		return propertyRepository.setProperty(group, key, value);
	}

	public List<DataProperty> getData() {
		return propertyRepository.getData();
	}

	public List<DataProperty> getData(String application) {
		return propertyRepository.getData(application);
	}

	public List<DataProperty> getData(String application, String instance) {
		return propertyRepository.getData(application, instance);
	}

	public List<DataProperty> getData(String application, String instance, String group) {
		return propertyRepository.getData(application, instance, group);
	}

	public DataProperty getData(String application, String instance, String group, String key) {
		return propertyRepository.getData(application, instance, group, key);
	}

	public Boolean setData(DataProperty property) {
		return propertyRepository.setData(property);
	}

	public Boolean setData(String application, String instance, String group, String key, String value) {
		return propertyRepository.setData(application, instance, group, key, value);
	}

}
