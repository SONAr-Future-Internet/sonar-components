package br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow;

import java.util.List;

//Manually generated using http://127.0.0.1:8181/onos/v1/docs/#/
public class ONOSFlowGroup {
	//mandatory
	private List<ONOSFlow> flows;

	public List<ONOSFlow> getFlows() {
		return flows;
	}

	public void setFlows(List<ONOSFlow> flows) {
		this.flows = flows;
	}
}
