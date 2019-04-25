package br.ufu.facom.mehar.sonar.core.model.configuration;

import java.util.Properties;
import java.util.Set;

import br.ufu.facom.mehar.sonar.core.model.service.Policy;

public class Circuit {
	private Set<Segment> segments;
	private Set<Policy> policies;
	private Properties properties;
	
	public Set<Segment> getSegments() {
		return segments;
	}
	public void setSegments(Set<Segment> segments) {
		this.segments = segments;
	}
	public Set<Policy> getPolicies() {
		return policies;
	}
	public void setPolicies(Set<Policy> policies) {
		this.policies = policies;
	}
	public Properties getProperties() {
		return properties;
	}
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
}
