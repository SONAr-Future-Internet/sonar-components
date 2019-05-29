package br.ufu.facom.mehar.sonar.core.model.configuration;

import java.util.Map;
import java.util.UUID;

import br.ufu.facom.mehar.sonar.core.model.topology.Element;

//Configuration record
public class Configuration {
	// Target for deploying this configuration
	private Element element;
	private UUID idElement;

	// Order of configuration (useful for sorting)
	private Long order;

	// Type of configuration
	private ConfigurationType type;

	// Generic command for configuration
	private String command;
	
	// Params to instructions implementation
	private Map<String, Object> parameterMap;
	
	// Flow (specific for flow configutatio)
	private Flow flow;
	
	// Identification of configuration in elements : e.g. flowId
	private String identification;

	public Element getElement() {
		return element;
	}

	public void setElement(Element element) {
		this.element = element;
	}

	public UUID getIdElement() {
		return idElement;
	}

	public void setIdElement(UUID idElement) {
		this.idElement = idElement;
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

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public Flow getFlow() {
		return flow;
	}

	public void setFlow(Flow flow) {
		this.flow = flow;
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

	@Override
	public String toString() {
		return "Configuration [element=" + element + ", idElement=" + idElement + ", order=" + order + ", type=" + type
				+ ", command=" + command + ", parameterMap=" + parameterMap + ", flow=" + flow + ", identification="
				+ identification + "]";
	}
}
