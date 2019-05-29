package br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow;

//Manually generated using http://127.0.0.1:8181/onos/v1/docs/#/
public class ONOSFlowRecord {
	private Integer priority;
	private Integer timeout;
	private Boolean isPermanent;
	private String deviceId;
	private String id;
	private String flowId;
	private String tableId;
	private String appId;
	private Integer groupId;
	private String state;
	private Long life;
	private Long packets;
	private Long bytes;
	private String liveType;
	private Long lastSeen;

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

	public void setId(String id) {
		this.id = id;
	}

	public String getTableId() {
		return tableId;
	}

	public void setTableId(String tableId) {
		this.tableId = tableId;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public Integer getGroupId() {
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Long getLife() {
		return life;
	}

	public void setLife(Long life) {
		this.life = life;
	}

	public Long getPackets() {
		return packets;
	}

	public void setPackets(Long packets) {
		this.packets = packets;
	}

	public Long getBytes() {
		return bytes;
	}

	public void setBytes(Long bytes) {
		this.bytes = bytes;
	}

	public String getLiveType() {
		return liveType;
	}

	public void setLiveType(String liveType) {
		this.liveType = liveType;
	}

	public Long getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(Long lastSeen) {
		this.lastSeen = lastSeen;
	}

	public String getFlowId() {
		return flowId;
	}

	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}

	public String getId() {
		return id;
	}
}