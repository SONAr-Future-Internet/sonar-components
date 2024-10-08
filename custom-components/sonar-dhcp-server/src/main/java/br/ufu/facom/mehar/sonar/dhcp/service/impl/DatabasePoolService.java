package br.ufu.facom.mehar.sonar.dhcp.service.impl;

import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.client.ndb.configuration.SonarProperties;
import br.ufu.facom.mehar.sonar.client.ndb.service.PropertyDataService;
import br.ufu.facom.mehar.sonar.client.ndb.service.TopologyDataService;
import br.ufu.facom.mehar.sonar.core.model.property.DataProperty;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;
import br.ufu.facom.mehar.sonar.core.util.IPUtils;
import br.ufu.facom.mehar.sonar.dhcp.service.PoolService;

@Service("database")
public class DatabasePoolService implements PoolService{
	@Autowired
	private MemoryPoolService memoryPoolService;
	
	@Autowired
	private PropertyDataService propertyService;
	
	@Autowired
	private TopologyDataService topologyService;

	@PostConstruct
	public void init() {
		List<DataProperty> dataPropertyList = propertyService.getData(SonarProperties.APPLICATION_DHCP_SERVER, SonarProperties.INSTANCE_SHARED, SonarProperties.GROUP_DHCP_MAC_TO_IP);
		for(DataProperty dataproperty : dataPropertyList) {
			memoryPoolService.getMacToIp().put(dataproperty.getKey(), dataproperty.getValue());
			memoryPoolService.getBusyIPs().add(dataproperty.getValue());
		}
		
		Set<Port> portList = topologyService.getPortsWithIP();
		for(Port port : portList) {
			if(port.getIpAddress() != null) {
				memoryPoolService.getMacToIp().put(port.getMacAddress(), port.getIpAddress());
				memoryPoolService.getBusyIPs().add(port.getIpAddress());
				
			}
		}
	}

	@Override
	public String getIP(String macAddress) {
		String normalizedMacAddress = IPUtils.normalizeMAC(macAddress);
		String ip = this.memoryPoolService.getIP(normalizedMacAddress);
		propertyService.setData(SonarProperties.APPLICATION_DHCP_SERVER, SonarProperties.INSTANCE_SHARED, SonarProperties.GROUP_DHCP_MAC_TO_IP, normalizedMacAddress, ip);
		return ip;
	}

	@Override
	public void register(String macAddress, String ip) {
		String normalizedMacAddress = IPUtils.normalizeMAC(macAddress);
		this.memoryPoolService.register(normalizedMacAddress, ip);
		propertyService.setData(SonarProperties.APPLICATION_DHCP_SERVER, SonarProperties.INSTANCE_SHARED, SonarProperties.GROUP_DHCP_MAC_TO_IP, normalizedMacAddress, ip);
	}

	@Override
	public Boolean isAvailable(String ip) {
		return this.memoryPoolService.isAvailable(ip);
	}

	@Override
	public Boolean isRegistered(String macAddress, String ip) {
		String normalizedMacAddress = IPUtils.normalizeMAC(macAddress);
		return this.memoryPoolService.isRegistered(normalizedMacAddress, ip);
	}
}
