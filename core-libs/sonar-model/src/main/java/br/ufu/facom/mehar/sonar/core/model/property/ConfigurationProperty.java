package br.ufu.facom.mehar.sonar.core.model.property;

public class ConfigurationProperty {
	private String group;
	private String key;
	private String value;
	
	public ConfigurationProperty() {
		super();
	}

	public ConfigurationProperty(String group, String key, String value) {
		this();
		this.group = group;
		this.key = key;
		this.value = value;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
