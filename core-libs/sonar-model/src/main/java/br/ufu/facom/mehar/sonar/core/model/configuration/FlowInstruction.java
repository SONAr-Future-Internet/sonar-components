package br.ufu.facom.mehar.sonar.core.model.configuration;

import java.util.UUID;

public class FlowInstruction {
	private FlowInstructionType type;
	
	private String value;
	
	private UUID refValue;
	
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

	public FlowInstruction(FlowInstructionType type, String value, UUID refValue) {
		this(type,value);
		this.refValue = refValue;
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

	public UUID getRefValue() {
		return refValue;
	}

	public void setRefValue(UUID refValue) {
		this.refValue = refValue;
	}
}
