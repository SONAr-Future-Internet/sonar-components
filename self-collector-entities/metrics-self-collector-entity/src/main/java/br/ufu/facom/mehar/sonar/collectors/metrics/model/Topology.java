package br.ufu.facom.mehar.sonar.collectors.metrics.model;

import java.io.Serializable;
import java.util.List;

public class Topology implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<Cluster> clusters;

	public List<Cluster> getClusters() {
		return clusters;
	}

	public void setClusters(List<Cluster> clusters) {
		this.clusters = clusters;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{clusters: ");
		sb.append(clusters);
		sb.append("}");
		return sb.toString();
	}
	
}
