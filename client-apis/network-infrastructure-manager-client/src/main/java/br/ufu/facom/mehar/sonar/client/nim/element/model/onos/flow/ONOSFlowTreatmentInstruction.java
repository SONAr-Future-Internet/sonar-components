package br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow;

//Manually generated using http://127.0.0.1:8181/onos/v1/docs/#/
public class ONOSFlowTreatmentInstruction {
	private String type;
	private Object port;
	private Integer tableId;
	private Integer groupId;
	private Integer meterId;
	private Integer queueId;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Object getPort() {
		return port;
	}
	public void setPort(Object port) {
		this.port = port;
	}
	public Integer getTableId() {
		return tableId;
	}
	public void setTableId(Integer tableId) {
		this.tableId = tableId;
	}
	public Integer getGroupId() {
		return groupId;
	}
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}
	public Integer getMeterId() {
		return meterId;
	}
	public void setMeterId(Integer meterId) {
		this.meterId = meterId;
	}
	public Integer getQueueId() {
		return queueId;
	}
	public void setQueueId(Integer queueId) {
		this.queueId = queueId;
	}
}
