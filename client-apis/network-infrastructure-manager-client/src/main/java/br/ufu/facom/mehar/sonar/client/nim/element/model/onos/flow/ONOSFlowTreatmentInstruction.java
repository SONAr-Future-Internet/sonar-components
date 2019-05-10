package br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow;

//Manually generated using http://127.0.0.1:8181/onos/v1/docs/#/
public class ONOSFlowTreatmentInstruction {
	//mandatory
	private String type;
	//mandatory
	private String port;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
}
