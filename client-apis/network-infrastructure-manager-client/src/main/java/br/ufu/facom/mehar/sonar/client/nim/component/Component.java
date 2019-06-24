package br.ufu.facom.mehar.sonar.client.nim.component;

public enum Component {
	//Basic Data and Communication COmponentes
	DistributedNetworkDatabase("NDB","distributed-network-database"),
	NetworkEventManager("NEM", "network-event-manager"),
	
	//Data Collectors
	TopologySelfCollectorEntity("TSCoE", "topology-self-collector-entity"),
	MetricsSelfCollectorEntity("MSCoE", "metrics-self-collector-entity"),
	
	//Organizing Entities
	SelfConfigurationEntity("SCE", "self-configuraion-entity"),
	SelfHealingEntity("SHE", "self-healing-entity"),
	SelfOptimizationEntity("SOE", "self-optimization-entity"),
	SelfProtectionEntity("SPE", "self-protection-entity"),
	
	DHCPServer("DHCP", "dhcp-server"),
	SDNController("SDNCrtl", "sdn-controller"), 
	ControllerInterceptor("CI","controller-interceptor");
	
	private String acronym;
	private String key;
	
	private Component(String acronym, String key) {
		this.acronym = acronym;
		this.key = key;
	}

	public String getAcronym() {
		return acronym;
	}

	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public static Component getByKey(String key) {
		for(Component component : Component.values()) {
			if(component.getKey().equals(key)) {
				return component;
			}
		}
		return null;
	}
	
}
