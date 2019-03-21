package br.ufu.facom.mehar.sonar.boot.model;

import java.net.InetAddress;
import java.util.Date;

public class Device {
	public enum SourceType{
		DHCP, LLDP
	}
	public enum State{
		NEW, CONFIGURED
	}
	
	public enum Type{
		UNKNOWN, SWITCH, HOST
	}
	
	private InetAddress ipAddress;
	private String macAddress;
	private SourceType source;
	private State state;
	private Type type;
	private String vendor;
	private String model;
	private Date registerTime;
	
	public Device(InetAddress ipAddress, String macAddress, SourceType source) {
		super();
		this.ipAddress = ipAddress;
		this.macAddress = macAddress;
		this.source = source;
		this.type = Type.UNKNOWN;
		this.state = State.NEW;
		this.registerTime = new Date();
	}
	
	public Date getRegisterTime() {
		return registerTime;
	}



	public void setRegisterTime(Date registerTime) {
		this.registerTime = registerTime;
	}



	public InetAddress getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	public SourceType getSource() {
		return source;
	}
	public void setSource(SourceType source) {
		this.source = source;
	}
	public State getState() {
		return state;
	}
	public void setState(State state) {
		this.state = state;
	}
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public String getVendor() {
		return vendor;
	}
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
}
