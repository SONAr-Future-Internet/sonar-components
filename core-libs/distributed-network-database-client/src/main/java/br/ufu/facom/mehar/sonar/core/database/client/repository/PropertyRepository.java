package br.ufu.facom.mehar.sonar.core.database.client.repository;

import java.util.List;

import br.ufu.facom.mehar.sonar.core.model.property.DataProperty;
import br.ufu.facom.mehar.sonar.core.model.property.ConfigurationProperty;

public interface PropertyRepository {
	/*
	 * Configuration of SONAr components
	 * Ex.
	 * 		SELF-HEALING-ENTITY.MAX_OCUPATION_THRESHOLD=90
	 */
	public List<ConfigurationProperty> getConfiguration();
	public List<ConfigurationProperty> getConfiguration(String group);
	public ConfigurationProperty getConfiguration(String group, String key);
	
	public Boolean setProperty(ConfigurationProperty property);
	public Boolean setProperty(String group, String key, Object value);
	
	
	/*
	 * Generic Aplication 'key-value' data.
	 * Ex. 
	 * 		DHCP.NET1.IPPOOL-TABLE.02:42:02:7c:9e:ad=192.168.0.2
	 * 		DHCP.NET1.IPPOOL-CONFIG.RANGE-START=192.168.0.2
	 * 		DHCP.NET1.IPPOOL-CONFIG.RANGE-FINISH=192.168.0.3
	 */
	public List<DataProperty> getData();
	public List<DataProperty> getData(String application);
	public List<DataProperty> getData(String application, String instance);
	public List<DataProperty> getData(String application, String instance, String group);
	public DataProperty getData(String application, String instance, String group, String key);
	
	public Boolean setData(DataProperty property);
	public Boolean setData(String application, String group, String key, Object value);
}
