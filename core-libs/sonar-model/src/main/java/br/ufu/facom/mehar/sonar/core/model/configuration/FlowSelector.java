package br.ufu.facom.mehar.sonar.core.model.configuration;

public class FlowSelector {
	// key
	private FlowSelector type;

	// value
	private String value;

	public FlowSelector getType() {
		return type;
	}

	public void setType(FlowSelector type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
