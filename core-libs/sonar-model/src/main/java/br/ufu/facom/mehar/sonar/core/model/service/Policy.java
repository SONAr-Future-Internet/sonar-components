package br.ufu.facom.mehar.sonar.core.model.service;

public enum Policy {
	BANDWIDTH_GUARANTEED("NFR01","The allocated bandwidth for the communication must be guaranteed"),
	BANDWIDTH_LIMITED("NFR02","The bandwidth allocated for the communication must be limited"),
	BANDWIDTH_DYNAMIC("NFR03","The bandwidth allocated for the communication must be dynamic"),
	LIMIT_TIME("NFR04","The use of links must be limited by time"),
	LIMIT_DATA("NFR05","The use of links must be limited by data"),
	LIMIT_COST("NFR06","The use of links must be limited by cost"),
	CHANNEL_RELIABLE("NFR07","The channel established for the communication must be reliable (error free)"),
	CHANNEL_FAST("NFR08","The channel established for the communication must be fast (minimum latency)"),
	CHANNEL_CHEAP("NFR09","The channel established for the communication must be cheap (low cost)"),
	CHANNEL_SAFE("NFR10","The channel established for the communication must be safe (less incidence of attacks)"),
	CHANNEL_OPTIMAL("NFR11","The channel established for the communication must be optimized (better cost-effective)"),
	RESTRICTION_DEVICE("NFR12","The communication channel must be restrictedto certain devices"),
	RESTRICTION_APPLICATION("NFR13","The communication channel must be restrictedto certain applications"),
	RESTRICTION_CONTENT("NFR14","The communication channel must be restrictedto certain types of content"),
	RESTRICTION_TECHNOLOGY("NFR15","The communication channel must be restrictedto certain technologies"),
	RESTRICTION_TERRITORY("NFR16","The communication channel must be restrictedto certain territories");
	
	private String id;
	private String description;
	
	private Policy(String id, String description) {
		this.id = id;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
