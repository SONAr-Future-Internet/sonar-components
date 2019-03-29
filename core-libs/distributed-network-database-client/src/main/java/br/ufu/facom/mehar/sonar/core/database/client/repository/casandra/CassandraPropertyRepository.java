package br.ufu.facom.mehar.sonar.core.database.client.repository.casandra;

import java.util.List;

import org.springframework.stereotype.Repository;

import br.ufu.facom.mehar.sonar.core.database.client.repository.PropertyRepository;
import br.ufu.facom.mehar.sonar.core.model.property.ConfigurationProperty;
import br.ufu.facom.mehar.sonar.core.model.property.DataProperty;

@Repository
public class CassandraPropertyRepository extends CassandraGenericRepository implements PropertyRepository {
	private static final String KEYSPACE = "property";
	private static final String CONFIGURATION_COLECTION = "configuration";
	private static final String DATA_COLECTION = "data";

	@Override
	public List<ConfigurationProperty> getConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ConfigurationProperty> getConfiguration(String group) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConfigurationProperty getConfiguration(String group, String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean setProperty(ConfigurationProperty property) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean setProperty(String group, String key, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DataProperty> getData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DataProperty> getData(String application) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DataProperty> getData(String application, String instance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DataProperty> getData(String application, String instance, String group) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataProperty getData(String application, String instance, String group, String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean setData(DataProperty property) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean setData(String application, String group, String key, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

}
