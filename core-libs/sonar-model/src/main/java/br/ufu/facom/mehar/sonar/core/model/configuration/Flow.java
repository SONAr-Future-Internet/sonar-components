package br.ufu.facom.mehar.sonar.core.model.configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Flow {
	private Integer priority;
	private Integer timeout;
	private Boolean isPermanent;
	private Set<FlowSelector> selectors;
	private Set<FlowInstruction> instructions;
	
	public Flow() {
		super();
		this.selectors = new HashSet<FlowSelector>();
		this.instructions = new HashSet<FlowInstruction>();
		this.priority = 4000;
		this.isPermanent = Boolean.TRUE;
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
}
