package br.ufu.facom.mehar.sonar.core.model.configuration;

import java.util.Set;

public class Flow {
	private Set<FlowSelector> selectors;
	private Set<FlowInstruction> instructions;
	
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
}
