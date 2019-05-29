package br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow;

//Manually generated using http://127.0.0.1:8181/onos/v1/docs/#/
public class ONOSFlow {
	// mandatory
	private Integer priority;
	// mandatory?
	private Integer timeout;
	// mandatory
	private Boolean isPermanent;
	// mandatory
	private String deviceId;
	// optional
	private ONOSFlowTreatment treatment;
	// optional
	private ONOSFlowSelector selector;

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public Boolean getIsPermanent() {
		return isPermanent;
	}

	public void setIsPermanent(Boolean isPermanent) {
		this.isPermanent = isPermanent;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public ONOSFlowTreatment getTreatment() {
		return treatment;
	}

	public void setTreatment(ONOSFlowTreatment treatment) {
		this.treatment = treatment;
	}

	public ONOSFlowSelector getSelector() {
		return selector;
	}

	public void setSelector(ONOSFlowSelector selector) {
		this.selector = selector;
	}
}