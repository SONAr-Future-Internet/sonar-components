package br.ufu.facom.mehar.sonar.core.model.configuration;

public class FlowSelector {
	private FlowSelectorType type;

	private String value;
	

	public FlowSelector() {
		super();
	}

	public FlowSelector(FlowSelectorType ethType, String value) {
		this();
		this.type = ethType;
		this.value = value;
	}

	public FlowSelectorType getType() {
		return type;
	}

	public void setType(FlowSelectorType type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
