package br.ufu.facom.mehar.sonar.client.dndb.repository;

import java.util.List;
import java.util.UUID;

import br.ufu.facom.mehar.sonar.core.model.topology.Domain;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;

public interface TopologyRepository {
	//Save / Update / Delete
	public Domain save(Domain domain);
	public Element save(Element element);
	public Port save(Port port);
	
	public Domain update(Domain domain);
	public Element update(Element element);
	public Port update(Port port);
	
	public Boolean delete(Domain domain);
	public Boolean delete(Element element);
	public Boolean delete(Port port);
	public Boolean deleteDomains();
	public Boolean deleteElements();
	public Boolean deletePorts();
	public Boolean deleteDomainById(UUID idDomain);
	public Boolean deleteElementById(UUID idElement);
	public Boolean deletePortById(UUID idPort);
	public Boolean deleteElementByIdDomain(UUID idDomain);
	public Boolean deletePortByIdElement(UUID idElement);
	
	//Domain Queries
	public List<Domain> getDomains();
	public Domain getDomainById(Domain idDomain);
	
	//Element Queries
	public List<Element> getElements();
	public List<Element> getElementsByIdDomain(UUID idDomain);
	public List<Element> getElementByHostname(String name);
	public List<Element> getElementByIPAddress(String address);
	public Element getElementById(UUID idElement);
	
	//Port Queries
	public List<Port> getPorts();
	public List<Port> getPortsByIdElement(UUID idElement);
	public Port getPortById(UUID idPort);
	public Port getPortByMacAddress(String macAddress);
}
