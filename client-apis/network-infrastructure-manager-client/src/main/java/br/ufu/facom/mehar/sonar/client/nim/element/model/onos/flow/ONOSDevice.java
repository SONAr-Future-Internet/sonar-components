package br.ufu.facom.mehar.sonar.client.nim.element.model.onos.flow;

import java.util.List;
import java.util.Map;

public class ONOSDevice {
	private String id; 			//"of:00000a4fa7c2d743"
	private String type; 		//"SWITCH"
  	private Boolean available;	//true,
	private String role;		//"MASTER",
	private String mfr;			//"Nicira, Inc.",
	private String hw; 			//"Open vSwitch",
	private String sw; 			//"2.10.1",
	private String serial; 		//"None",
	private String driver; 		//"ovs",
	private String chassisId; 	//"a4fa7c2d743",
	private String lastUpdate; //"1557502324651",
	
	private Map<String, String> annotations;
	//	channelId: "192.168.0.2:48762"
	//	managementAddress: "192.168.0.2"
	//	protocol: "OF_13"
	
	private List<ONOSPort> ports;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Boolean getAvailable() {
		return available;
	}
	public void setAvailable(Boolean available) {
		this.available = available;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getMfr() {
		return mfr;
	}
	public void setMfr(String mfr) {
		this.mfr = mfr;
	}
	public String getHw() {
		return hw;
	}
	public void setHw(String hw) {
		this.hw = hw;
	}
	public String getSw() {
		return sw;
	}
	public void setSw(String sw) {
		this.sw = sw;
	}
	public String getSerial() {
		return serial;
	}
	public void setSerial(String serial) {
		this.serial = serial;
	}
	public String getDriver() {
		return driver;
	}
	public void setDriver(String driver) {
		this.driver = driver;
	}
	public String getChassisId() {
		return chassisId;
	}
	public void setChassisId(String chassisId) {
		this.chassisId = chassisId;
	}
	public String getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(String lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	public Map<String, String> getAnnotations() {
		return annotations;
	}
	public void setAnnotations(Map<String, String> annotations) {
		this.annotations = annotations;
	}
	public List<ONOSPort> getPorts() {
		return ports;
	}
	public void setPorts(List<ONOSPort> ports) {
		this.ports = ports;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ONOSDevice other = (ONOSDevice) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
