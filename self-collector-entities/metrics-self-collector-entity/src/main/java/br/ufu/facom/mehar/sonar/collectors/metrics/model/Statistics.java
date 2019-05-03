package br.ufu.facom.mehar.sonar.collectors.metrics.model;

import java.io.Serializable;

public class Statistics implements Serializable {

	private static final long serialVersionUID = 1L;
	// Successful transmit and receive counters
	private Long rx_packets;
	private Long rx_bytes;
	private Long tx_packets;
	private Long tx_bytes;
	// Receive errors:
	private Long rx_dropped;
	private Long rx_frame_err;
	private Long rx_over_err;
	private Long rx_crc_err;
	private Long rx_errors;
	// Transmit errors
	private Long tx_dropped;
	private Long collisions;
	private Long tx_errors;

	public Statistics(Long rx_packets, Long rx_bytes, Long tx_packets, Long tx_bytes, Long rx_dropped,
			Long rx_frame_err, Long rx_over_err, Long rx_crc_err, Long rx_errors, Long tx_dropped, Long collisions,
			Long tx_errors) {
		this.rx_packets = rx_packets;
		this.rx_bytes = rx_bytes;
		this.tx_packets = tx_packets;
		this.tx_bytes = tx_bytes;
		this.rx_dropped = rx_dropped;
		this.rx_frame_err = rx_frame_err;
		this.rx_over_err = rx_over_err;
		this.rx_crc_err = rx_crc_err;
		this.rx_errors = rx_errors;
		this.tx_dropped = tx_dropped;
		this.collisions = collisions;
		this.tx_errors = tx_errors;
	}

	public Long getRx_packets() {
		return rx_packets;
	}

	public void setRx_packets(Long rx_packets) {
		this.rx_packets = rx_packets;
	}

	public Long getRx_bytes() {
		return rx_bytes;
	}

	public void setRx_bytes(Long rx_bytes) {
		this.rx_bytes = rx_bytes;
	}

	public Long getTx_packets() {
		return tx_packets;
	}

	public void setTx_packets(Long tx_packets) {
		this.tx_packets = tx_packets;
	}

	public Long getTx_bytes() {
		return tx_bytes;
	}

	public void setTx_bytes(Long tx_bytes) {
		this.tx_bytes = tx_bytes;
	}

	public Long getRx_dropped() {
		return rx_dropped;
	}

	public void setRx_dropped(Long rx_dropped) {
		this.rx_dropped = rx_dropped;
	}

	public Long getRx_frame_err() {
		return rx_frame_err;
	}

	public void setRx_frame_err(Long rx_frame_err) {
		this.rx_frame_err = rx_frame_err;
	}

	public Long getRx_over_err() {
		return rx_over_err;
	}

	public void setRx_over_err(Long rx_over_err) {
		this.rx_over_err = rx_over_err;
	}

	public Long getRx_crc_err() {
		return rx_crc_err;
	}

	public void setRx_crc_err(Long rx_crc_err) {
		this.rx_crc_err = rx_crc_err;
	}

	public Long getRx_errors() {
		return rx_errors;
	}

	public void setRx_errors(Long rx_errors) {
		this.rx_errors = rx_errors;
	}

	public Long getTx_dropped() {
		return tx_dropped;
	}

	public void setTx_dropped(Long tx_dropped) {
		this.tx_dropped = tx_dropped;
	}

	public Long getCollisions() {
		return collisions;
	}

	public void setCollisions(Long collisions) {
		this.collisions = collisions;
	}

	public Long getTx_errors() {
		return tx_errors;
	}

	public void setTx_errors(Long tx_errors) {
		this.tx_errors = tx_errors;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"rx_packets\":" + rx_packets);
		sb.append(", \"rx_bytes\":" + rx_bytes);
		sb.append(", \"tx_packets\":" + tx_packets);
		sb.append(", \"tx_bytes\":" + tx_bytes);
		sb.append(", \"rx_dropped\":" + rx_dropped);
		sb.append(", \"rx_frame_err\":" + rx_frame_err);
		sb.append(", \"rx_over_err\":" + rx_over_err);
		sb.append(", \"rx_crc_err\":" + rx_crc_err);
		sb.append(", \"rx_errors\":" + rx_errors);
		sb.append(", \"tx_dropped\":" + tx_dropped);
		sb.append(", \"collisions\":" + collisions);
		sb.append(", \"tx_errors\":" + tx_errors + "}");
		return sb.toString();
	}

}
