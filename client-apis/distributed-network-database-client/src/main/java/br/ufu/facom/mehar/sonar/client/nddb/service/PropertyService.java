package br.ufu.facom.mehar.sonar.client.nddb.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.client.nddb.repository.PropertyRepository;
import br.ufu.facom.mehar.sonar.core.model.property.ConfigurationProperty;
import br.ufu.facom.mehar.sonar.core.model.property.DataProperty;

@Service
public class PropertyService{
	
	@Autowired
	private PropertyRepository propertyRepository;

	
	public List<ConfigurationProperty> getConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public List<ConfigurationProperty> getConfiguration(String group) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public ConfigurationProperty getConfiguration(String group, String key) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Boolean setProperty(ConfigurationProperty property) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Boolean setProperty(String group, String key, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public List<DataProperty> getData() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public List<DataProperty> getData(String application) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public List<DataProperty> getData(String application, String instance) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public List<DataProperty> getData(String application, String instance, String group) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public DataProperty getData(String application, String instance, String group, String key) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Boolean setData(DataProperty property) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Boolean setData(String application, String group, String key, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

}
