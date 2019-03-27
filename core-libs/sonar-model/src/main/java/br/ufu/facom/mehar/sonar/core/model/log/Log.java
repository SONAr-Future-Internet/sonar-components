package br.ufu.facom.mehar.sonar.core.model.log;

import br.ufu.facom.mehar.sonar.core.model.topology.Element;

public class Log {
	private Element element;
	private LogSeverity severity;
	private String value;

	public Element getElement() {
		return element;
	}

	public void setElement(Element element) {
		this.element = element;
	}

	public LogSeverity getSeverity() {
		return severity;
	}

	public void setSeverity(LogSeverity severity) {
		this.severity = severity;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
