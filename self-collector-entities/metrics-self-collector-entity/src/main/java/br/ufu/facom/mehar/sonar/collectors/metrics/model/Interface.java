package br.ufu.facom.mehar.sonar.collectors.metrics.model;

import java.io.Serializable;

public class Interface implements Serializable {

	private static final long serialVersionUID = 1L;
	private String name;
	private Statistics statistics;

	public Interface() {
	}
	
	public Interface(String name, Statistics statistics) {
		this.name = name;
		this.statistics = statistics;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Statistics getStatistics() {
		return statistics;
	}

	public void setStatistics(Statistics statistics) {
		this.statistics = statistics;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"name\":\"" + name + "\"");
		sb.append(", \"statistics\":" + statistics + "}");
		return sb.toString();
	}

}
