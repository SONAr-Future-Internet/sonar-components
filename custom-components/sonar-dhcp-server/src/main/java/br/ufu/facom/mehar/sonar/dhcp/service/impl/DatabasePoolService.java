package br.ufu.facom.mehar.sonar.dhcp.service.impl;

import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.client.ndb.service.PropertyService;
import br.ufu.facom.mehar.sonar.client.ndb.service.TopologyService;
import br.ufu.facom.mehar.sonar.core.model.property.DataProperty;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;
import br.ufu.facom.mehar.sonar.core.util.IPUtils;
import br.ufu.facom.mehar.sonar.dhcp.service.PoolService;

@Service("database")
public class DatabasePoolService implements PoolService{
	private static String APPLICATION_NAME = "sonar-dhcp-server";
	private static String INSTANCE_NAME = IPUtils.searchActiveInterfaceAddress().getAddress().getHostAddress();
	private static String GROUP = "IP-POOL-TABLE";
	
	@Autowired
	private MemoryPoolService memoryPoolService;
	
	@Autowired
	private PropertyService propertyService;
	
	@Autowired
	private TopologyService topologyService;

	@PostConstruct
	public void init() {
		List<DataProperty> dataPropertyList = propertyService.getData(APPLICATION_NAME, INSTANCE_NAME, GROUP);
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
		propertyService.setData(APPLICATION_NAME, INSTANCE_NAME, GROUP, normalizedMacAddress, ip);
		return ip;
	}
}
