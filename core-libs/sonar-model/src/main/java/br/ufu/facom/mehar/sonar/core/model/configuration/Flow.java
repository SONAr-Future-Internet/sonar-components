package br.ufu.facom.mehar.sonar.core.model.configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Flow {
	private Integer priority;
	private Integer timeout;
	private Boolean isPermanent;
	private Set<FlowSelector> selectors;
	private Set<FlowInstruction> instructions;

	private String deviceId;
	private UUID deviceRef;
	
	public Flow() {
		super();
		this.selectors = new HashSet<FlowSelector>();
		this.instructions = new HashSet<FlowInstruction>();
		this.priority = 4000;
		this.isPermanent = Boolean.TRUE;
	}
	
	public Flow(String deviceId, UUID deviceRef, FlowSelector flowSelector, FlowInstruction flowInstruction) {
		this();
		this.deviceId = deviceId;
		this.deviceRef = deviceRef;
		this.selectors.add(flowSelector);
		this.instructions.add(flowInstruction);
	}

	public Flow(String deviceId, UUID deviceRef, List<FlowSelector> selectorsList, List<FlowInstruction> instructionsList) {
		this();
		this.deviceId = deviceId;
		this.deviceRef = deviceRef;
		this.selectors.addAll(selectorsList);
		this.instructions.addAll(instructionsList);
	}
	
	public Flow(String deviceId, List<FlowSelector> selectorsList, List<FlowInstruction> instructionsList) {
		this();
		this.deviceId = deviceId;
		this.selectors.addAll(selectorsList);
		this.instructions.addAll(instructionsList);
	}
	
	public Flow(String deviceId, FlowSelector flowSelector, FlowInstruction flowInstruction) {
		this();
		this.deviceId = deviceId;
		this.selectors.add(flowSelector);
		this.instructions.add(flowInstruction);
	}
	
	public Flow(FlowSelector flowSelector, FlowInstruction flowInstruction) {
		this();
		this.selectors.add(flowSelector);
		this.instructions.add(flowInstruction);
	}

	public Flow(List<FlowSelector> selectorsList, List<FlowInstruction> instructionsList) {
		this();
		this.selectors.addAll(selectorsList);
		this.instructions.addAll(instructionsList);
	}

	public Set<FlowSelector> getSelectors() {
		return selectors;
	}
	public void setSelectors(Set<FlowSelector> selectors) {
		this.selectors = selectors;
	}
	public Set<FlowInstruction> getInstructions() {
		return instructions;
	}
	public void setInstructions(Set<FlowInstruction> instructions) {
		this.instructions = instructions;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public Boolean getIsPermanent() {
		return isPermanent;
	}

	public void setIsPermanent(Boolean isPermanent) {
		this.isPermanent = isPermanent;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public UUID getDeviceRef() {
		return deviceRef;
	}

	public void setDeviceRef(UUID deviceref) {
		this.deviceRef = deviceref;
	}
	
	
}
