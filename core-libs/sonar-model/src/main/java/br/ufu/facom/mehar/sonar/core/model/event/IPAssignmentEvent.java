package br.ufu.facom.mehar.sonar.core.model.event;

import java.util.Date;

public class IPAssignmentEvent {
	//Basic Data
	private String ip;
	private String mac;
	
	//Context
	private Date moment;
	
	//OpenFlow fields
	private String portInIp;
	private String portInPort;
	
	public IPAssignmentEvent() {
		super();
	}
	
	public IPAssignmentEvent(String mac, String ip) {
		this();
		this.moment = new Date();
		this.mac = mac;
		this.ip = ip;
	}
	
	public IPAssignmentEvent(String mac, String ip, String portInIp, String portInPort) {
		this(mac,ip);
		this.portInIp = portInIp;
		this.portInPort = portInPort;
		
	}
	
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public Date getMoment() {
		return moment;
	}
	public void setMoment(Date moment) {
		this.moment = moment;
	}
	public String getPortInIp() {
		return portInIp;
	}
	public void setPortInIp(String portInIp) {
		this.portInIp = portInIp;
	}
	public String getPortInPort() {
		return portInPort;
	}
	public void setPortInPort(String portInPort) {
		this.portInPort = portInPort;
	}
	
	
}
