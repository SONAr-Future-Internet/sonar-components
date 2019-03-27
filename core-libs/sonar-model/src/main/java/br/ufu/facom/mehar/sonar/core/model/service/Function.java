package br.ufu.facom.mehar.sonar.core.model.service;

public enum Function {
	UNICAST_INTRA_DOMAIN("FR01","The network must allow communication betweena  pair  of  entities  (unicast)  of  the  same  domain  (intra-domain)"),
	UNICAST_INTER_DOMAIN("FR02","The network must allow communication betweena  pair  of  entities  (unicast)  of  different  domains  (inter-domain)"),
	MULTICAST_INTRA_DOMAIN("FR03","The network should allow group communicationof  entities  (multicast)  communication  within  the  samedomain (intra-domain)"),
	MULTICAST_INTER_DOMAIN("FR04","The network should allow group communicationof entities (multicast) of different domains (inter-domain)"),
	BROADCAST("FR05","The network must allow communication betweenan  entity  and  all  other  entities  of  the  same  domain(broadcast)"),
	ANYCAST_INTRA_DOMAIN("FR06","The network must allow communication betweenan entity and one or more entities of a group at the samedomain (anycast intra-domain)"),
	ANYCAST_INTER_DOMAIN("FR07","The network must allow communication betweenan  entity  and  one  or  more  entities  of  a  group  among different domains (anycast inter-domain)");
	
	private String id;
	private String description;
	
	private Function(String id, String description) {
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
