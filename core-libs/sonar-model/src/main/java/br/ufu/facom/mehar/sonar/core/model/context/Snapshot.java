package br.ufu.facom.mehar.sonar.core.model.context;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import br.ufu.facom.mehar.sonar.core.model.metric.ElementMetric;
import br.ufu.facom.mehar.sonar.core.model.metric.FlowMetric;
import br.ufu.facom.mehar.sonar.core.model.metric.PortMetric;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;
/*
 * Snapshot of 'Network' moment independent of SONAr Services Abstraction
 */
public class Snapshot extends Moment{
	//Infrastructure
	private List<Element> topology;
	
	//Metrics
	private Map<UUID, ElementMetric> elementMetrics;
	private Map<UUID, PortMetric> portMetrics;
	private Map<UUID, FlowMetric> flowMetrics;

	//Calculated using all costs considering the benefits
	private Long generalEvaluation;
	
	//Costs
	//Capex : estimated considering investmnet and obsolescence
	private Double capexEstimatedCost;
	//Opex : energy usage and recurrent costs
	private Double energyCost;
	private Double recurringCosts;
	
	//Benefits
	private Double averageLatency;
	private Double percentErrors;
	private Integer countAttacks;
	
	public List<Element> getTopology() {
		return topology;
	}
	public void setTopology(List<Element> topology) {
		this.topology = topology;
	}
	public Map<UUID, ElementMetric> getElementMetrics() {
		return elementMetrics;
	}
	public void setElementMetrics(Map<UUID, ElementMetric> elementMetrics) {
		this.elementMetrics = elementMetrics;
	}
	public Map<UUID, PortMetric> getPortMetrics() {
		return portMetrics;
	}
	public void setPortMetrics(Map<UUID, PortMetric> portMetrics) {
		this.portMetrics = portMetrics;
	}
	public Map<UUID, FlowMetric> getFlowMetrics() {
		return flowMetrics;
	}
	public void setFlowMetrics(Map<UUID, FlowMetric> flowMetrics) {
		this.flowMetrics = flowMetrics;
	}
	public Long getGeneralEvaluation() {
		return generalEvaluation;
	}
	public void setGeneralEvaluation(Long generalEvaluation) {
		this.generalEvaluation = generalEvaluation;
	}
	public Double getCapexEstimatedCost() {
		return capexEstimatedCost;
	}
	public void setCapexEstimatedCost(Double capexEstimatedCost) {
		this.capexEstimatedCost = capexEstimatedCost;
	}
	public Double getEnergyCost() {
		return energyCost;
	}
	public void setEnergyCost(Double energyCost) {
		this.energyCost = energyCost;
	}
	public Double getRecurringCosts() {
		return recurringCosts;
	}
	public void setRecurringCosts(Double recurringCosts) {
		this.recurringCosts = recurringCosts;
	}
	public Double getAverageLatency() {
		return averageLatency;
	}
	public void setAverageLatency(Double averageLatency) {
		this.averageLatency = averageLatency;
	}
	public Double getPercentErrors() {
		return percentErrors;
	}
	public void setPercentErrors(Double percentErrors) {
		this.percentErrors = percentErrors;
	}
	public Integer getCountAttacks() {
		return countAttacks;
	}
	public void setCountAttacks(Integer countAttacks) {
		this.countAttacks = countAttacks;
	}
}
