package br.ufu.facom.mehar.sonar.core.model.topology;

import br.ufu.facom.mehar.sonar.core.model.topology.type.LinkEvent;
import br.ufu.facom.mehar.sonar.core.model.topology.type.LinkState;

public class Link {
	private Port portA;
	private Port portB;
	
	private LinkState state;
	private LinkEvent event;
	
	public Link(Port portA, Port portB, LinkEvent event) {
		this.portA = portA;
		this.portB = portB;
		this.event = event;
	}
	public Port getPortA() {
		return portA;
	}
	public void setPortA(Port portA) {
		this.portA = portA;
	}
	public Port getPortB() {
		return portB;
	}
	public void setPortB(Port portB) {
		this.portB = portB;
	}
	public LinkState getState() {
		return state;
	}
	public void setState(LinkState state) {
		this.state = state;
	}
	public LinkEvent getEvent() {
		return event;
	}
	public void setEvent(LinkEvent event) {
		this.event = event;
	}
	@Override
	public String toString() {
		return "{"+event+":"+portA+" <-> "+portB+"}";
	}
}
