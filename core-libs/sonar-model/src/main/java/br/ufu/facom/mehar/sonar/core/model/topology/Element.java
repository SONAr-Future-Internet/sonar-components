package br.ufu.facom.mehar.sonar.core.model.topology;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

// Generic Element abstraction (types: Device, Host, Server, Entity)
public class Element {
	// Primary Key
	private UUID idElement;

	// ManyToOne (Element -> Domain)
	private UUID idDomain;

	// Name / Hostname
	private Long name;
	
	// Type of Element
	private String typeElement;


	// Management IPs
	private Set<String> managementIPAddressList;

	// Discovery Fields
	private Date lastDicoveredAt; // when?
	private String lastDicoveredBy; // who?
	private String lastDicoveredMethod; // how?
	private String lastDicoveredSource; // from?

	// Atributes
	private Integer memory; // ram memory
	private Integer cores; // processor cores
	private Double clock; // processor clock
	private Long disk; // disk size
	private Double cost; // generic cost per second
	private Double energy; // energy usage per second

	// ManyToOne (Element -> Domain)
	private transient Domain domain;
	
	// OneToMany Mapping (Element -> Ports)
	private Set<Port> portList;
	
	// Types Enumeration as Constants
	public static final String TYPE_DEVICE = "DEVICE";
	public static final String TYPE_HOST = "HOST";
	public static final String TYPE_ENTITY = "ENTITY";
	public static final String TYPE_SERVER = "SERVER";

	// Discovery Methods
	public static final String DISCOVERY_SNMP_LLDP = "SNMP_LLDP";
	public static final String DISCOVERY_SDN_CONTROLER = "SDN_CONTROLLER";

	public UUID getIdElement() {
		return idElement;
	}

	public void setIdElement(UUID idElement) {
		this.idElement = idElement;
	}

	public UUID getIdDomain() {
		return idDomain;
	}

	public void setIdDomain(UUID idDomain) {
		this.idDomain = idDomain;
	}

	public String getTypeElement() {
		return typeElement;
	}

	public void setTypeElement(String typeElement) {
		this.typeElement = typeElement;
	}

	public Long getName() {
		return name;
	}

	public void setName(Long name) {
		this.name = name;
	}

	public Set<String> getManagementIPAddressList() {
		return managementIPAddressList;
	}

	public void setManagementIPAddressList(Set<String> managementIPAddressList) {
		this.managementIPAddressList = managementIPAddressList;
	}

	public Date getLastDicoveredAt() {
		return lastDicoveredAt;
	}

	public void setLastDicoveredAt(Date lastDicoveredAt) {
		this.lastDicoveredAt = lastDicoveredAt;
	}

	public String getLastDicoveredBy() {
		return lastDicoveredBy;
	}

	public void setLastDicoveredBy(String lastDicoveredBy) {
		this.lastDicoveredBy = lastDicoveredBy;
	}

	public String getLastDicoveredMethod() {
		return lastDicoveredMethod;
	}

	public void setLastDicoveredMethod(String lastDicoveredMethod) {
		this.lastDicoveredMethod = lastDicoveredMethod;
	}

	public String getLastDicoveredSource() {
		return lastDicoveredSource;
	}

	public void setLastDicoveredSource(String lastDicoveredSource) {
		this.lastDicoveredSource = lastDicoveredSource;
	}

	

	public Double getClock() {
		return clock;
	}

	public void setClock(Double clock) {
		this.clock = clock;
	}

	public Long getDisk() {
		return disk;
	}

	public void setDisk(Long disk) {
		this.disk = disk;
	}

	public Integer getMemory() {
		return memory;
	}

	public void setMemory(Integer memory) {
		this.memory = memory;
	}

	public Integer getCores() {
		return cores;
	}

	public void setCores(Integer cores) {
		this.cores = cores;
	}

	public Double getCost() {
		return cost;
	}

	public void setCost(Double cost) {
		this.cost = cost;
	}

	public Double getEnergy() {
		return energy;
	}

	public void setEnergy(Double energy) {
		this.energy = energy;
	}

	public Set<Port> getPortList() {
		return portList;
	}

	public void setPortList(Set<Port> portList) {
		this.portList = portList;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

}
