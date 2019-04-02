package br.ufu.facom.mehar.sonar.client.cim;

public enum SONArComponents {
	DistributedNetworkDatabase("DNDB"),
	NetworkEventManager("NEM");
	
	private String acronym;
	
	private SONArComponents(String acronym) {
		this.acronym = acronym;
	}

	public String getAcronym() {
		return acronym;
	}

	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}
}
