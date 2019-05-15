package br.ufu.facom.mehar.sonar.core.model.configuration;

public class FlowInstruction {
	private FlowInstructionType type;
	
	private String value;
	
	

	public FlowInstruction() {
		super();
	}
	
	public FlowInstruction(FlowInstructionType type) {
		this();
		this.type = type;
	}

	public FlowInstruction(FlowInstructionType type, String value) {
		this(type);
		this.value = value;
	}

	public FlowInstructionType getType() {
		return type;
	}

	public void setType(FlowInstructionType type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
