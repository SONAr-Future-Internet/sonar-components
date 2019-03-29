package br.ufu.facom.mehar.sonar.collectors.metrics.model;

import java.io.Serializable;

public class Port implements Serializable {

	private static final long serialVersionUID = 1L;
	private int port;
	private long packetsReceived;
	private long packetsSent;
	private long bytesReceived;
	private long bytesSent;
	private long packetsRxDropped;
	private long packetsTxDropped;
	private long packetsRxErrors;
	private long packetsTxErrors;
	private long durationSec;
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public long getPacketsReceived() {
		return packetsReceived;
	}

	public void setPacketsReceived(long packetsReceived) {
		this.packetsReceived = packetsReceived;
	}

	public long getPacketsSent() {
		return packetsSent;
	}

	public void setPacketsSent(long packetsSent) {
		this.packetsSent = packetsSent;
	}

	public long getBytesReceived() {
		return bytesReceived;
	}

	public void setBytesReceived(long bytesReceived) {
		this.bytesReceived = bytesReceived;
	}

	public long getBytesSent() {
		return bytesSent;
	}

	public void setBytesSent(long bytesSent) {
		this.bytesSent = bytesSent;
	}

	public long getPacketsRxDropped() {
		return packetsRxDropped;
	}

	public void setPacketsRxDropped(long packetsRxDropped) {
		this.packetsRxDropped = packetsRxDropped;
	}

	public long getPacketsTxDropped() {
		return packetsTxDropped;
	}

	public void setPacketsTxDropped(long packetsTxDropped) {
		this.packetsTxDropped = packetsTxDropped;
	}

	public long getPacketsRxErrors() {
		return packetsRxErrors;
	}

	public void setPacketsRxErrors(long packetsRxErrors) {
		this.packetsRxErrors = packetsRxErrors;
	}

	public long getPacketsTxErrors() {
		return packetsTxErrors;
	}

	public void setPacketsTxErrors(long packetsTxErrors) {
		this.packetsTxErrors = packetsTxErrors;
	}

	public long getDurationSec() {
		return durationSec;
	}

	public void setDurationSec(long durationSec) {
		this.durationSec = durationSec;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{port: ");
		sb.append(port);
		sb.append(", packetsReceived: ");
		sb.append(packetsReceived);
		sb.append(", packetsSent: ");
		sb.append(packetsSent);
		sb.append(", bytesReceived: ");
		sb.append(bytesReceived);
		sb.append(", bytesSent: ");
		sb.append(bytesSent);
		sb.append(", packetsRxDropped: ");
		sb.append(packetsRxDropped);
		sb.append(", packetsRxErrors: ");
		sb.append(packetsRxErrors);
		sb.append(", packetsTxErrors: ");
		sb.append(packetsTxErrors);
		sb.append(", durationSec: ");
		sb.append(durationSec);
		sb.append("}");
		return sb.toString();
	}

}
