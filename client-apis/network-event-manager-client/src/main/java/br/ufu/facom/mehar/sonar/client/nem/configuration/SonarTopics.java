package br.ufu.facom.mehar.sonar.client.nem.configuration;

public class SonarTopics {
	public static final String TOPIC_TOPOLOGY = "sonar.topology.#";
	public static final String TOPIC_TOPOLOGY_ELEMENT = "sonar.topology.element.*";
	public static final String TOPIC_TOPOLOGY_ELEMENT_ADDED = "sonar.topology.element.added";
	public static final String TOPIC_TOPOLOGY_ELEMENT_REMOVED = "sonar.topology.element.removed";
	public static final String TOPIC_TOPOLOGY_ELEMENT_CHANGED = "sonar.topology.element.changed";
	public static final String TOPIC_TOPOLOGY_PORT = "sonar.topology.port.*";
	public static final String TOPIC_TOPOLOGY_PORT_ADDED = "sonar.topology.port.added";
	public static final String TOPIC_TOPOLOGY_PORT_REMOVED = "sonar.topology.port.removed";
	public static final String TOPIC_TOPOLOGY_PORT_CHANGED = "sonar.topology.port.changed";
	public static final String TOPIC_TOPOLOGY_PORT_IP_ASSIGNED = "sonar.topology.port.ipAssigned";
	
	public static final String TOPIC_METRICS = "sonar.metrics.#";
	public static final String TOPIC_METRICS_ELEMENT = "sonar.metrics.element.*";
	public static final String TOPIC_METRICS_ELEMENT_INFO = "sonar.metrics.element.info";
	public static final String TOPIC_METRICS_ELEMENT_WARN = "sonar.metrics.element.warn";
	public static final String TOPIC_METRICS_PORT = "sonar.metrics.port.*";
	public static final String TOPIC_METRICS_PORT_INFO = "sonar.metrics.port.info";
	public static final String TOPIC_METRICS_PORT_WARN = "sonar.metrics.port.warn";
	public static final String TOPIC_METRICS_FLOW = "sonar.metrics.flow.*";
	public static final String TOPIC_METRICS_FLOW_INFO = "sonar.metrics.flow.info";
	public static final String TOPIC_METRICS_FLOW_WARN = "sonar.metrics.flow.warn";
}
