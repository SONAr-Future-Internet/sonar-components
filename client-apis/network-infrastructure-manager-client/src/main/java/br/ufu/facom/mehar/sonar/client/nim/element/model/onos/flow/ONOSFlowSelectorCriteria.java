package br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow;

//Manually generated using http://127.0.0.1:8181/onos/v1/docs/#/ and https://wiki.onosproject.org/display/ONOS/Flow+Rules
public class ONOSFlowSelectorCriteria {
	//key
	private String type;
	
	//value
	private String ethType;
	private String mac;
	private String port;
	private String metadata;
	private String vlanId;
	private String priority;
	private String innerVlanId;
	private String innerPriority;
	private Integer ipDscp;
	private Integer ipEcn;
	private Integer protocol;
	private String ip;
	private Integer tcpPort;
	private Integer udpPort;
	private Integer sctpPort;
	private String icmpType;
	private Integer icmpCode;
	private Integer flowlabel;
	private Integer icmpv6Type;
	private String targetAddress;
	private Integer label;
	private Integer exthdrFlags;
	private Integer lambda;
	private String gridType;
	private Integer channelSpacing;
	private Integer spacingMultiplier;
	private Integer slotGranularity;
	private Integer tunnelId;
	private Integer ochSignalId;
	private Integer ochSignalType;
	private Integer tributaryPortNumber;
	private byte[] tributarySlotBitmap;
	private Integer tributarySlotLen;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getEthType() {
		return ethType;
	}
	public void setEthType(String ethType) {
		this.ethType = ethType;
	}
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getMetadata() {
		return metadata;
	}
	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}
	public String getVlanId() {
		return vlanId;
	}
	public void setVlanId(String vlanId) {
		this.vlanId = vlanId;
	}
	public String getPriority() {
		return priority;
	}
	public void setPriority(String priority) {
		this.priority = priority;
	}
	public String getInnerVlanId() {
		return innerVlanId;
	}
	public void setInnerVlanId(String innerVlanId) {
		this.innerVlanId = innerVlanId;
	}
	public String getInnerPriority() {
		return innerPriority;
	}
	public void setInnerPriority(String innerPriority) {
		this.innerPriority = innerPriority;
	}
	public Integer getIpDscp() {
		return ipDscp;
	}
	public void setIpDscp(Integer ipDscp) {
		this.ipDscp = ipDscp;
	}
	public Integer getIpEcn() {
		return ipEcn;
	}
	public void setIpEcn(Integer ipEcn) {
		this.ipEcn = ipEcn;
	}
	public Integer getProtocol() {
		return protocol;
	}
	public void setProtocol(Integer protocol) {
		this.protocol = protocol;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public Integer getTcpPort() {
		return tcpPort;
	}
	public void setTcpPort(Integer tcpPort) {
		this.tcpPort = tcpPort;
	}
	public Integer getUdpPort() {
		return udpPort;
	}
	public void setUdpPort(Integer udpPort) {
		this.udpPort = udpPort;
	}
	public Integer getSctpPort() {
		return sctpPort;
	}
	public void setSctpPort(Integer sctpPort) {
		this.sctpPort = sctpPort;
	}
	public String getIcmpType() {
		return icmpType;
	}
	public void setIcmpType(String icmpType) {
		this.icmpType = icmpType;
	}
	public Integer getIcmpCode() {
		return icmpCode;
	}
	public void setIcmpCode(Integer icmpCode) {
		this.icmpCode = icmpCode;
	}
	public Integer getFlowlabel() {
		return flowlabel;
	}
	public void setFlowlabel(Integer flowlabel) {
		this.flowlabel = flowlabel;
	}
	public Integer getIcmpv6Type() {
		return icmpv6Type;
	}
	public void setIcmpv6Type(Integer icmpv6Type) {
		this.icmpv6Type = icmpv6Type;
	}
	public String getTargetAddress() {
		return targetAddress;
	}
	public void setTargetAddress(String targetAddress) {
		this.targetAddress = targetAddress;
	}
	public Integer getLabel() {
		return label;
	}
	public void setLabel(Integer label) {
		this.label = label;
	}
	public Integer getExthdrFlags() {
		return exthdrFlags;
	}
	public void setExthdrFlags(Integer exthdrFlags) {
		this.exthdrFlags = exthdrFlags;
	}
	public Integer getLambda() {
		return lambda;
	}
	public void setLambda(Integer lambda) {
		this.lambda = lambda;
	}
	public String getGridType() {
		return gridType;
	}
	public void setGridType(String gridType) {
		this.gridType = gridType;
	}
	public Integer getChannelSpacing() {
		return channelSpacing;
	}
	public void setChannelSpacing(Integer channelSpacing) {
		this.channelSpacing = channelSpacing;
	}
	public Integer getSpacingMultiplier() {
		return spacingMultiplier;
	}
	public void setSpacingMultiplier(Integer spacingMultiplier) {
		this.spacingMultiplier = spacingMultiplier;
	}
	public Integer getSlotGranularity() {
		return slotGranularity;
	}
	public void setSlotGranularity(Integer slotGranularity) {
		this.slotGranularity = slotGranularity;
	}
	public Integer getTunnelId() {
		return tunnelId;
	}
	public void setTunnelId(Integer tunnelId) {
		this.tunnelId = tunnelId;
	}
	public Integer getOchSignalId() {
		return ochSignalId;
	}
	public void setOchSignalId(Integer ochSignalId) {
		this.ochSignalId = ochSignalId;
	}
	public Integer getOchSignalType() {
		return ochSignalType;
	}
	public void setOchSignalType(Integer ochSignalType) {
		this.ochSignalType = ochSignalType;
	}
	public Integer getTributaryPortNumber() {
		return tributaryPortNumber;
	}
	public void setTributaryPortNumber(Integer tributaryPortNumber) {
		this.tributaryPortNumber = tributaryPortNumber;
	}
	public byte[] getTributarySlotBitmap() {
		return tributarySlotBitmap;
	}
	public void setTributarySlotBitmap(byte[] tributarySlotBitmap) {
		this.tributarySlotBitmap = tributarySlotBitmap;
	}
	public Integer getTributarySlotLen() {
		return tributarySlotLen;
	}
	public void setTributarySlotLen(Integer tributarySlotLen) {
		this.tributarySlotLen = tributarySlotLen;
	}
}
