package br.ufu.facom.mehar.sonar.core.database.client.repository;

import java.util.List;

import br.ufu.facom.mehar.sonar.core.model.topology.Domain;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;

public interface TopologyRepository {
	//Save / Update / Delete
	public Domain save(Domain domain);
	public Element save(Element element);
	public Port save(Port element);
	public Domain update(Domain domain);
	public Element update(Element element);
	public Port update(Port element);
	public Boolean delete(Domain domain);
	public Boolean delete(Element element);
	public Boolean delete(Port port);
	
	//Domain Queries
	public Domain getDomainById(Domain idDomain);
	public Domain getDomainByIPAddress(String ip);
	
	//Element Queries
	public Element getElementById(Long idElement);
	public Element getElementByIPAddress(String address);
	public Element getElementByHostname(String address);
	public Element getElementByPortMacAddress(String macAddress);
	
	//Port Queries
	public Port getPortById(Long idPort);
	public Port getPortByMacAddress(String macAddress);
	public Port getByHostnameAndIfId(String hostname , String ifId);
	public Port getByIpAddressAndIfId(String ip, String ifId);
	public List<Port> getPortByElement(Element element);
	public List<Port> getPortByIdElement(Long idElement);
}
