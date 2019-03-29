package br.ufu.facom.mehar.sonar.core.model.sample;

import br.ufu.facom.mehar.sonar.core.model.topology.Port;

public class Sample {
	private Long flowId;
	private Port ingressPort;

	private String ipSrc;
	private Long portSrc;
	private String ipDst;
	private String portDst;

	private String protL3;
	private String protL4;
	private String protL5;

	private byte[] content;

	public Long getFlowId() {
		return flowId;
	}

	public void setFlowId(Long flowId) {
		this.flowId = flowId;
	}

	public Port getIngressPort() {
		return ingressPort;
	}

	public void setIngressPort(Port ingressPort) {
		this.ingressPort = ingressPort;
	}

	public String getIpSrc() {
		return ipSrc;
	}

	public void setIpSrc(String ipSrc) {
		this.ipSrc = ipSrc;
	}

	public Long getPortSrc() {
		return portSrc;
	}

	public void setPortSrc(Long portSrc) {
		this.portSrc = portSrc;
	}

	public String getIpDst() {
		return ipDst;
	}

	public void setIpDst(String ipDst) {
		this.ipDst = ipDst;
	}

	public String getPortDst() {
		return portDst;
	}

	public void setPortDst(String portDst) {
		this.portDst = portDst;
	}

	public String getProtL3() {
		return protL3;
	}

	public void setProtL3(String protL3) {
		this.protL3 = protL3;
	}

	public String getProtL4() {
		return protL4;
	}

	public void setProtL4(String protL4) {
		this.protL4 = protL4;
	}

	public String getProtL5() {
		return protL5;
	}

	public void setProtL5(String protL5) {
		this.protL5 = protL5;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

}
