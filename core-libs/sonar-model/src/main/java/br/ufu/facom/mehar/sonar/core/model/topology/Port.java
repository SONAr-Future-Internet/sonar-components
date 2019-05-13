package br.ufu.facom.mehar.sonar.core.model.topology;

import java.util.UUID;

import br.ufu.facom.mehar.sonar.core.model.topology.type.PortModeType;
import br.ufu.facom.mehar.sonar.core.model.topology.type.PortState;

// Port abstraction
public class Port {
	// Primary Key
	private UUID idPort;

	// ManyToOne (Port -> Element)
	private UUID idElement;

	// OneToOne (Port -> Port)
	private UUID remoteIdPort;

	// Local Fields
	private String portId;
	private String portName;
	private String macAddress;
	private String ipAddress;

	// Remote Identification
	private String remoteHostname;
	private String remotePortId;
	private String remotePortName;
	private String remoteMacAddress;
	private String remoteIpAddress;

	// Attributes
	private PortModeType mode; // Simplex, Half-Duplex e Full-Duplex
	private Integer speed; // Speed configured / max bandwidth
	private PortState adminState;
	private PortState state;

	// SDN Fields
	private String ofPort;

	// ManyToOne (Port -> Element)
	private transient Element element;
	// OneToOne (Port -> Port)
	private transient Port remotePort;

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

	public PortModeType getMode() {
		return mode;
	}

	public void setMode(PortModeType mode) {
		this.mode = mode;
	}

	public Integer getSpeed() {
		return speed;
	}

	public void setSpeed(Integer speed) {
		this.speed = speed;
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

	public String getPortId() {
		return portId;
	}

	public void setPortId(String portId) {
		this.portId = portId;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public String getRemotePortId() {
		return remotePortId;
	}

	public void setRemotePortId(String remotePortId) {
		this.remotePortId = remotePortId;
	}

	public String getRemotePortName() {
		return remotePortName;
	}

	public void setRemotePortName(String remotePortName) {
		this.remotePortName = remotePortName;
	}

	public String getRemoteHostname() {
		return remoteHostname;
	}

	public void setRemoteHostname(String remoteHostname) {
		this.remoteHostname = remoteHostname;
	}

	public String getOfPort() {
		return ofPort;
	}

	public void setOfPort(String ofPort) {
		this.ofPort = ofPort;
	}

	public PortState getAdminState() {
		return adminState;
	}

	public void setAdminState(PortState adminState) {
		this.adminState = adminState;
	}

	public PortState getState() {
		return state;
	}

	public void setState(PortState state) {
		this.state = state;
	}
}
