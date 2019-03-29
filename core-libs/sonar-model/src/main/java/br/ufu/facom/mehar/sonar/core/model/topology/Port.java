package br.ufu.facom.mehar.sonar.core.model.topology;

import java.util.UUID;

// Port abstraction
public class Port {
	// Primary Key
	private UUID idPort;

	// ManyToOne (Port -> Element)
	private UUID idElement;

	// OneToOne (Port -> Port)
	private UUID remoteIdPort;


	// Local Fields
	private String ifId;
	private String ifName;
	private String macAddress;
	private String ipAddress;

	// Remote Identification
	private String remoteIfId;
	private String remoteName;
	private String remoteMacAddress;
	private String remoteIpAddress;

	// Attributes
	private String mode; // Simplex, Half-Duplex e Full-Duplex
	private Long bandwidth; // Speed configured / max bandwidth
	
	// ManyToOne (Port -> Element)
	private transient Element element;
	// OneToOne (Port -> Port)
	private transient Port remotePort;

	// Modes
	public static final String MODE_SIMPLEX = "SIMPLEX";
	public static final String MODE_HALF_DUPLEX = "HALF_DUPLEX";
	public static final String MODE_FULL_DUPLEX = "FULL_DUPLEX";

	public UUID getIdPort() {
		return idPort;
	}

	public void setIdPort(UUID idPort) {
		this.idPort = idPort;
	}

	public UUID getIdElement() {
		return idElement;
	}

	public void setIdElement(UUID idElement) {
		this.idElement = idElement;
	}

	public UUID getRemoteIdPort() {
		return remoteIdPort;
	}

	public void setRemoteIdPort(UUID remoteIdPort) {
		this.remoteIdPort = remoteIdPort;
	}

	public String getIfId() {
		return ifId;
	}

	public void setIfId(String ifId) {
		this.ifId = ifId;
	}

	public String getIfName() {
		return ifName;
	}

	public void setIfName(String ifName) {
		this.ifName = ifName;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getRemoteIfId() {
		return remoteIfId;
	}

	public void setRemoteIfId(String remoteIfId) {
		this.remoteIfId = remoteIfId;
	}

	public String getRemoteName() {
		return remoteName;
	}

	public void setRemoteName(String remoteName) {
		this.remoteName = remoteName;
	}

	public String getRemoteMacAddress() {
		return remoteMacAddress;
	}

	public void setRemoteMacAddress(String remoteMacAddress) {
		this.remoteMacAddress = remoteMacAddress;
	}

	public String getRemoteIpAddress() {
		return remoteIpAddress;
	}

	public void setRemoteIpAddress(String remoteIpAddress) {
		this.remoteIpAddress = remoteIpAddress;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public Long getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(Long bandwidth) {
		this.bandwidth = bandwidth;
	}

	public Element getElement() {
		return element;
	}

	public void setElement(Element ownerElement) {
		this.element = ownerElement;
	}

	public Port getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(Port remotePort) {
		this.remotePort = remotePort;
	}
}
