package br.ufu.facom.mehar.sonar.core.model.metric;

// Port Metric record
public class PortMetric {
	//ManyToOne (PortMetric -> Port)
	private Long idPort;
	
	//Constants with prefix 'PORT_'
	private String key;
	
	//Value: Generic number
	private Double value;
	
	// General Metrics Keys
	public static final String PACKETS_ERRORS 		= "PACKETS_ERRORS";
	public static final String PACKETS_DROPS 		= "PACKETS_DROPS";
	public static final String BANDWIDTH_USAGE 		= "BANDWIDTH_USAGE";
	
	// Rx & Tx Metrics Keys
	public static final String PACKETS_RECEIVED 	= "PACKETS_RECEIVED";
	public static final String PACKETS_SENT 		= "PACKETS_SENT";
	public static final String BYTES_RECEIVED 		= "BYTES_RECEIVED";
	public static final String BYTES_SENT 			= "BYTES_SENT";
	public static final String PACKETS_RX_DROPPED	= "PACKETS_RX_DROPPED";
	public static final String PACKETS_TX_DROPPED	= "PACKETS_TX_DROPPED";
	public static final String PACKETS_RX_ERRORS 	= "PACKETS_RX_ERRORS";
	public static final String PACKETS_TX_ERRORS 	= "PACKETS_TX_ERRORS";
	
	public Long getIdPort() {
		return idPort;
	}

	public void setIdPort(Long idPort) {
		this.idPort = idPort;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}
}
