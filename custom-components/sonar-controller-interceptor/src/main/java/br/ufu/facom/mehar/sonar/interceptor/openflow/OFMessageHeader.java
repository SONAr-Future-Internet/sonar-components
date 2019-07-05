package br.ufu.facom.mehar.sonar.interceptor.openflow;

public class OFMessageHeader {
	private short version;
	private short type;
	private int length;
	private long transactionId;

	public OFMessageHeader(short version, short type, int length, long transactionId) {
		this.version = version;
		this.type = type;
		this.length = length;
		this.transactionId = transactionId;
	}

	public short getVersion() {
		return version;
	}

	public void setVersion(short version) {
		this.version = version;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(long transactionId) {
		this.transactionId = transactionId;
	}

}