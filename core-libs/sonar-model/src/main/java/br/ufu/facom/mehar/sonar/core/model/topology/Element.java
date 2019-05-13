package br.ufu.facom.mehar.sonar.core.model.topology;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import br.ufu.facom.mehar.sonar.core.model.topology.type.ElementType;

// Generic Element abstraction (types: Device, Host, Server, Entity)
public class Element {
	// Primary Key
	private UUID idElement;

	// ManyToOne (Element -> Domain)
	private UUID idDomain;

	// Name / Hostname
	private String name;

	// Type of Element
	private ElementType typeElement;

	// Management IPs
	private Set<String> managementIPAddressList;

	// Discovery Fields
	private Date lastDicoveredAt; // when?
	private String lastDicoveredBy; // who?
	private String lastDicoveredSource; // from?
	private String lastDicoveredMethod; // how?
	
	// SDN Fields
	private Set<String> ofControllers;
	private String ofDeviceId;

	// Atributes
	private Integer memory; // ram memory
	private Integer cores; // processor cores
	private Double clock; // processor clock
	private Long disk; // disk size
	private Double cost; // generic cost per second
	private Double energy; // energy usage per second
	
	// Product Fields
	private String manufacturer;
	private String product;
	private String software;
	
	// ManyToOne (Element -> Domain)
	private transient Domain domain;

	// OneToMany Mapping (Element -> Ports)
	private Set<Port> portList;

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

	public ElementType getTypeElement() {
		return typeElement;
	}

	public void setTypeElement(ElementType typeElement) {
		this.typeElement = typeElement;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
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

	public Set<String> getOfControllers() {
		return ofControllers;
	}

	public void setOfControllers(Set<String> ofControllers) {
		this.ofControllers = ofControllers;
	}

	public String getOfDeviceId() {
		return ofDeviceId;
	}

	public void setOfDeviceId(String ofDeviceId) {
		this.ofDeviceId = ofDeviceId;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getSoftware() {
		return software;
	}

	public void setSoftware(String software) {
		this.software = software;
	}
}
