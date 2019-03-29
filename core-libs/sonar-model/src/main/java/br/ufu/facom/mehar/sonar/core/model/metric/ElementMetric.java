package br.ufu.facom.mehar.sonar.core.model.metric;

// Element Metric record
public class ElementMetric {
	// ManyToOne (PortMetric -> Element)
	private Long idElement;
	
	// Constants with prefix 'PORT_'
	private String key;
	
	// Value: Generic number
	private Double value;
	
	// General Metrics Keys
	public static final String MEMORY_USAGE 		= "PACKETS_RECEIVED";
	public static final String PROCESSOR_USAGE 		= "PACKETS_SENT";
	public static final String ENERGY_USAGE 		= "BYTES_RECEIVED";
	
	// Device Metric keys
	public static final String PORTS_USED 			= "BYTES_SENT";
	public static final String PACKETS_ERRORS 		= "PACKETS_ERRORS";
	public static final String PACKETS_DROPS 		= "PACKETS_DROPS";
	public static final String BANDWIDTH_USAGE 		= "BANDWIDTH_USAGE";
	
	public Long getIdElement() {
		return idElement;
	}

	public void setIdElement(Long idElement) {
		this.idElement = idElement;
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
