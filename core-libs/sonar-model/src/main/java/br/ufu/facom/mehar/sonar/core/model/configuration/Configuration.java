package br.ufu.facom.mehar.sonar.core.model.configuration;

import java.util.Map;

import br.ufu.facom.mehar.sonar.core.model.topology.Element;

//Configuration record
public class Configuration {
	// Target for deploying this configuration
	private Element target;

	// Order of configuration (useful for sorting)
	private Long order;

	// Type of configuration
	private ConfigurationType type;

	// Generic instruction for configuration
	private String instruction;
	
	// Identification of configuration in elements : e.g. flowId
	private String identification;

	// Params to instructions implementation
	private Map<String, Object> parameterMap;

	public Element getTarget() {
		return target;
	}

	public void setTarget(Element target) {
		this.target = target;
	}

	public Long getOrder() {
		return order;
	}

	public void setOrder(Long order) {
		this.order = order;
	}

	public ConfigurationType getType() {
		return type;
	}

	public void setType(ConfigurationType type) {
		this.type = type;
	}

	public String getInstruction() {
		return instruction;
	}

	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}

	public Map<String, Object> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map<String, Object> parameterMap) {
		this.parameterMap = parameterMap;
	}

	public String getIdentification() {
		return identification;
	}

	public void setIdentification(String identification) {
		this.identification = identification;
	}
}
