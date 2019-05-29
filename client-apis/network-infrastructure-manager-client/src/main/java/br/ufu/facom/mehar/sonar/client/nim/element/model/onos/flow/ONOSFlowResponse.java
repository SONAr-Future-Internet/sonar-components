package br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow;

import java.util.List;

//Manually generated using http://127.0.0.1:8181/onos/v1/docs/#/
public class ONOSFlowResponse {
	//mandatory
	private List<ONOSFlowRecord> flows;

	public List<ONOSFlowRecord> getFlows() {
		return flows;
	}

	public void setFlows(List<ONOSFlowRecord> flows) {
		this.flows = flows;
	}
}
