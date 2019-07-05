package br.ufu.facom.mehar.sonar.interceptor.openflow;

public class OFMessagePayload {
	private byte[] data;

	public OFMessagePayload(byte[] data) {
		super();
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
