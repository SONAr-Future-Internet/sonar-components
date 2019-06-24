package br.ufu.facom.mehar.sonar.client.nem.configuration;

public class SonarTopics {
	public static final String TOPIC_TOPOLOGY = "sonar.topology.#";
	public static final String TOPIC_TOPOLOGY_ELEMENT = "sonar.topology.element.#";
	public static final String TOPIC_TOPOLOGY_ELEMENT_ADDED = "sonar.topology.element.added";
	public static final String TOPIC_TOPOLOGY_ELEMENT_REMOVED = "sonar.topology.element.removed";
	public static final String TOPIC_TOPOLOGY_ELEMENT_CHANGED = "sonar.topology.element.changed.#";
	public static final String TOPIC_TOPOLOGY_ELEMENT_CHANGED_DETAILS = "sonar.topology.element.changed.details";
	public static final String TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE = "sonar.topology.element.changed.state.*";
	public static final String TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_IP_ASSIGNED_BY_DHCP = "sonar.topology.element.changed.state.ip-assigned-by-dhcp";
	public static final String TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_REFERRED_BY_CONTROLLER = "sonar.topology.element.changed.state.referred-by-controller";
	public static final String TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_CONNECTED_TO_TOPOLOGY = "sonar.topology.element.changed.state.connected-to-topology";
	public static final String TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_WAITING_ROUTES = "sonar.topology.element.changed.state.waiting-routes";
	public static final String TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_WAITING_DISCOVERY = "sonar.topology.element.changed.state.waiting-discovery";
	public static final String TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_WAITING_CONTROLLER_ASSIGNMENT = "sonar.topology.element.changed.state.waiting-controller-assignment";
	public static final String TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_WAITING_CONTROLLER_CONNECTION = "sonar.topology.element.changed.state.waiting-controller-connection";
	public static final String TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_WAITING_CONFIGURATION = "sonar.topology.element.changed.state.waiting-configuration";
	public static final String TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_DISCOVERED = "sonar.topology.element.changed.state.discovered";
	public static final String TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_CONFIGURED = "sonar.topology.element.changed.state.configured";
	public static final String TOPIC_TOPOLOGY_ELEMENT_CHANGED_STATE_DISCONNECTED = "sonar.topology.element.changed.state.disconnected";

	public static final String TOPIC_TOPOLOGY_PORT = "sonar.topology.port.*";
	public static final String TOPIC_TOPOLOGY_PORT_ADDED = "sonar.topology.port.added";
	public static final String TOPIC_TOPOLOGY_PORT_REMOVED = "sonar.topology.port.removed";
	public static final String TOPIC_TOPOLOGY_PORT_CHANGED = "sonar.topology.port.changed";
	public static final String TOPIC_TOPOLOGY_LINKS = "sonar.topology.links.*";
	public static final String TOPIC_TOPOLOGY_LINKS_CHANGED = "sonar.topology.links.changed";
	
	public static final String TOPIC_BOOT = "sonar.boot.#";
	public static final String TOPIC_BOOT_START = "sonar.boot.start.*";
	public static final String TOPIC_BOOT_FINISH = "sonar.boot.finish.*";
	public static final String TOPIC_BOOT_START_DISCOVERY_STAGE = "sonar.boot.start.discovery";
	public static final String TOPIC_BOOT_FINISH_DISCOVERY_STAGE = "sonar.boot.finish.discovery";
	public static final String TOPIC_BOOT_START_ROUTING_STAGE = "sonar.boot.start.routing";
	public static final String TOPIC_BOOT_FINISH_ROUTING_STAGE = "sonar.boot.finish.routing";
	public static final String TOPIC_BOOT_START_CONFIGURATION_STAGE = "sonar.boot.start.configuration";
	public static final String TOPIC_BOOT_FINISH_CONFIGURATION_STAGE = "sonar.boot.finish.configuration";
	
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
	
	public static final String TOPIC_SERVICE = "sonar.service.*";
	public static final String TOPIC_SERVICE_ADDED = "sonar.service.added";
	public static final String TOPIC_SERVICE_REMOVED = "sonar.service.added";
	public static final String TOPIC_SERVICE_CHANGED = "sonar.service.added";
	
	public static final String TOPIC_DHCP = "sonar.dhcp.*";
	public static final String TOPIC_DHCP_IP_ASSIGNED = "sonar.dhcp.ip-assigned";
	
	public static final String TOPIC_INTERCEPTOR = "sonar.interceptor.#";
	public static final String TOPIC_INTERCEPTOR_NEW_CONNECTION = "sonar.interceptor.new-connection";
	public static final String TOPIC_INTERCEPTOR_PACKET_IN_DHCP = "sonar.interceptor.packet-in.dhcp";
	public static final String TOPIC_INTERCEPTOR_CALL_PACKET_OUT = "sonar.interceptor.call.packet-out";
	
	public static final String TOPIC_SCE_CALL_BOOT = "sonar.sce.call.boot";
	public static final String TOPIC_SCE_CALLBACK_BOOT = "sonar.sce.callback.boot";
	
}
