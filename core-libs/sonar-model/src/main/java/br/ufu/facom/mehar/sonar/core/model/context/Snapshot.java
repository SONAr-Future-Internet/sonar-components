package br.ufu.facom.mehar.sonar.core.model.context;

import java.util.List;
import java.util.Map;

import br.ufu.facom.mehar.sonar.core.model.metric.ElementMetric;
import br.ufu.facom.mehar.sonar.core.model.metric.PortMetric;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;

public class Snapshot {
	private Moment moment;

	List<Element> topology;

	private Map<Long, ElementMetric> elementMetrics;
	private Map<Long, PortMetric> portMetrics;

	private Long evaluation;

	public Moment getMoment() {
		return moment;
	}

	public void setMoment(Moment moment) {
		this.moment = moment;
	}

	public List<Element> getTopology() {
		return topology;
	}

	public void setTopology(List<Element> topology) {
		this.topology = topology;
	}

	public Map<Long, ElementMetric> getElementMetrics() {
		return elementMetrics;
	}

	public void setElementMetrics(Map<Long, ElementMetric> elementMetrics) {
		this.elementMetrics = elementMetrics;
	}

	public Map<Long, PortMetric> getPortMetrics() {
		return portMetrics;
	}

	public void setPortMetrics(Map<Long, PortMetric> portMetrics) {
		this.portMetrics = portMetrics;
	}

	public Long getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(Long evaluation) {
		this.evaluation = evaluation;
	}

}
