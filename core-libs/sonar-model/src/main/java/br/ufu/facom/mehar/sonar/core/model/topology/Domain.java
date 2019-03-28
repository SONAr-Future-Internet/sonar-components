package br.ufu.facom.mehar.sonar.core.model.topology;

import java.util.Set;
import java.util.UUID;

// Network Partition abstraction 
public class Domain {
	// Primary Key
	private UUID idDomain;

	// Name
	private String name;

	// Domain defined with networkAddress and networkMask
	private String networkAddress;
	private String networkMask;

	// Domain defined with a IP Range
	private String ipRangeStart;
	private String ipRangeFinish;
	
	// OneToMany (Domain -> Element)
	private Set<Element> elementList;

	public UUID getIdDomain() {
		return idDomain;
	}

	public void setIdDomain(UUID idDomain) {
		this.idDomain = idDomain;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNetworkAddress() {
		return networkAddress;
	}

	public void setNetworkAddress(String networkAddress) {
		this.networkAddress = networkAddress;
	}

	public String getNetworkMask() {
		return networkMask;
	}

	public void setNetworkMask(String networkMask) {
		this.networkMask = networkMask;
	}

	public String getIpRangeStart() {
		return ipRangeStart;
	}

	public void setIpRangeStart(String ipRangeStart) {
		this.ipRangeStart = ipRangeStart;
	}

	public String getIpRangeFinish() {
		return ipRangeFinish;
	}

	public void setIpRangeFinish(String ipRangeFinish) {
		this.ipRangeFinish = ipRangeFinish;
	}

	public Set<Element> getElementList() {
		return elementList;
	}

	public void setElementList(Set<Element> elementList) {
		this.elementList = elementList;
	}
}
