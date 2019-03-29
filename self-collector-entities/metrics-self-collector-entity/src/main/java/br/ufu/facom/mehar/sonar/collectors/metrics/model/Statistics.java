package br.ufu.facom.mehar.sonar.collectors.metrics.model;

import java.io.Serializable;
import java.util.List;

public class Statistics implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<Device> statistics;
	
	public List<Device> getStatistics() {
		return statistics;
	}

	public void setStatistics(List<Device> statistics) {
		this.statistics = statistics;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{statistics: ");
		sb.append(statistics);
		sb.append("}");
		return sb.toString();
	}
	
}
