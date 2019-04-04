package br.ufu.facom.mehar.sonar.core.model.property;

public class DataProperty {
	private String application;
	private String instance;
	private String group;
	private String key;
	private String value;



	public DataProperty() {
		super();
	}

	public DataProperty(String application, String instance, String group, String key, String value) {
		this();
		this.application = application;
		this.instance = instance;
		this.group = group;
		this.key = key;
		this.value = value;
	}

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
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
