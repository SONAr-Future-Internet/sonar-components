package br.ufu.facom.mehar.sonar.client.ndb.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.client.ndb.exception.DataValidationException;
import br.ufu.facom.mehar.sonar.client.ndb.repository.TopologyRepository;
import br.ufu.facom.mehar.sonar.core.model.topology.Domain;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;
import br.ufu.facom.mehar.sonar.core.model.topology.type.ElementType;
import br.ufu.facom.mehar.sonar.core.util.IPUtils;
import br.ufu.facom.mehar.sonar.core.util.ObjectUtils;
import br.ufu.facom.mehar.sonar.core.util.Pair;

@Service
public class TopologyDataService {

	@Autowired
	private TopologyRepository repository;

	public Domain save(Domain domain) {
		this.validate(domain);
		return repository.save(domain);
	}

	public Element save(Element element) {
		this.validate(element);
		return repository.save(element);
	}

	public Port save(Port port) {
		this.validate(port);
		return repository.save(port);
	}

	private void validate(Domain domain) {
		if(domain.getNetworkAddress() != null && domain.getNetworkMask() != null) {
			for(Domain domainPersisted : this.getDomains()) {
				if(domainPersisted.getNetworkAddress() != null && domainPersisted.getNetworkMask() != null) {
					
					
					if(IPUtils.isInNetwork(domain.getNetworkAddress(), domainPersisted.getNetworkAddress(), domainPersisted.getNetworkMask()) || IPUtils.isInNetwork(domainPersisted.getNetworkAddress(), domain.getNetworkAddress(), domain.getNetworkMask())) {
						throw new DataValidationException("Domain with requested IP network already exists. "+ObjectUtils.toString(domainPersisted));
					}
				}
			}
		}else {
			if(domain.getIpRangeStart() != null && domain.getIpRangeFinish() != null) {
				for(Domain domainPersisted : this.getDomains()) {
					if(domainPersisted.getIpRangeStart() != null && domainPersisted.getIpRangeFinish() != null) {
						if(IPUtils.isInRange(domain.getIpRangeStart(), domainPersisted.getIpRangeStart(), domainPersisted.getIpRangeFinish()) || IPUtils.isInRange(domain.getIpRangeFinish(), domainPersisted.getIpRangeStart(), domainPersisted.getIpRangeFinish())) {
							throw new DataValidationException("Domain with requested IP range already exists. "+ObjectUtils.toString(domainPersisted));
						}
					}
				}
			}else {
				throw new DataValidationException("NetworkAddress + NetworkMask or IPRangeStart + IPRangeFinish is required.");
			}
		}
	}
	
	private void validate(Element element) {
		if(element.getTypeElement() == null) {
			throw new DataValidationException("Element is invalid! Please fill the 'typeElement' field.");
		}
	}
	
	private void validate(Port port) {
		if(port.getIdElement() == null) {
			throw new DataValidationException("Port is invalid! Please fill the 'idElement' field.");
		}
		if(port.getMacAddress() == null || port.getMacAddress().isEmpty()) {
			throw new DataValidationException("Port is invalid! Please fill the 'macAddress' field.");
		}
	}

	public Domain saveCascade(Domain domain) {
		Domain result = this.save(domain);
		Set<Element> resultElementList = new HashSet<Element>();
		for (Element element : domain.getElementList()) {
			Element resultElement = this.saveCascade(element);
			resultElement.setDomain(domain);
			resultElementList.add(resultElement);
		}
		result.setElementList(resultElementList);
		return result;
	}

	public Element saveCascade(Element element) {
		Element result = this.save(element);
		Set<Port> resultPortList = new HashSet<Port>();
		for (Port port : element.getPortList()) {
			Port resultPort = this.save(port);
			resultPort.setElement(element);
			resultPortList.add(resultPort);
		}
		result.setPortList(resultPortList);
		return result;
	}

	public Domain update(Domain domain) {
		return repository.update(domain);
	}

	public Element update(Element element) {
		return repository.update(element);
	}

	public Port update(Port port) {
		return repository.update(port);
	}

	public Domain updateCascade(Domain domain) {
		Set<Element> resultElementList = new HashSet<Element>();
		for (Element element : domain.getElementList()) {
			Element resultElement = this.update(element);
			resultElement.setDomain(domain);
			resultElementList.add(resultElement);
		}

		Domain result = this.update(domain);
		result.setElementList(resultElementList);

		return result;
	}

	public Element updateCascade(Element element) {
		Set<Port> resultPortList = new HashSet<Port>();
		for (Port port : element.getPortList()) {
			Port resultPort = this.update(port);
			resultPort.setElement(element);
			resultPortList.add(resultPort);
		}

		Element result = this.update(element);
		result.setPortList(resultPortList);
		return result;
	}

	public Boolean delete(Domain domain) {
		this.deleteElementByIdDomain(domain.getIdDomain());
		return repository.delete(domain);
	}

	public Boolean delete(Element element) {
		this.deletePortByIdElement(element.getIdElement());
		return repository.delete(element);
	}

	public Boolean delete(Port port) {
		return repository.delete(port);
	}

	public Boolean deleteDomains() {
		this.deleteElements();
		return repository.deleteDomains();
	}

	public Boolean deleteElements() {
		this.deletePorts();
		return repository.deleteElements();
	}

	public Boolean deletePorts() {
		return repository.deletePorts();
	}

	public Boolean deleteDomainById(UUID idDomain) {
		this.deleteElementByIdDomain(idDomain);
		return repository.deleteDomainById(idDomain);
	}

	public Boolean deleteElementById(UUID idElement) {
		this.deletePortByIdElement(idElement);
		return repository.deleteElementById(idElement);
	}

	public Boolean deletePortById(UUID idPort) {
		return repository.deletePortById(idPort);
	}

	public Boolean deleteElementByIdDomain(UUID idDomain) {
		for (Element element : this.getElementByIdDomain(idDomain)) {
			this.deletePortByIdElement(element.getIdElement());
		}
		return repository.deleteElementByIdDomain(idDomain);
	}

	public Boolean deletePortByIdElement(UUID idElement) {
		return repository.deletePortByIdElement(idElement);
	}

	public List<Domain> getDomains() {
		return repository.getDomains();
	}

	public Domain getDomainById(Domain idDomain) {
		return repository.getDomainById(idDomain);
	}

	public List<Element> getElements() {
		return repository.getElements();
	}

	public Element getElementById(UUID idElement) {
		return repository.getElementById(idElement);
	}

	public Element getElementByIP(String address) {
		return repository.getElementByIPAddress(address);
	}

	public List<Element> getElementsByHostname(String address) {
		return repository.getElementsByHostname(address);
	}

	public Set<Port> getPorts() {
		return repository.getPorts();
	}

	public Port getPortById(UUID idPort) {
		return repository.getPortById(idPort);
	}

	public Port getPortByMacAddress(String macAddress) {
		return repository.getPortByMacAddress(macAddress);
	}

	public Set<Port> getPortsByIdElement(UUID idElement) {
		return repository.getPortsByIdElement(idElement);
	}

	private List<Element> getElementByIdDomain(UUID idDomain) {
		return repository.getElementsByIdDomain(idDomain);
	}

	public Domain getDomainByIPAddress(String ip) {
		for (Domain domain : this.getDomains()) {
			if (domain.getNetworkAddress() != null && domain.getNetworkMask() != null) {

				if (IPUtils.isInNetwork(ip, domain.getNetworkAddress(), domain.getNetworkMask())) {
					return domain;
				}

			} else {
				if (domain.getIpRangeStart() != null && domain.getIpRangeFinish() != null) {
					if (IPUtils.isInRange(ip, domain.getIpRangeStart(), domain.getIpRangeFinish())) {
						return domain;
					}
				}
			}
		}
		return null;
	}

	public Pair<Element,Port> getElementAndPortByPortMacAddress(String macAddress) {
		Port port = repository.getPortByMacAddress(macAddress);
		if (port != null) {
			return new Pair<Element, Port>(repository.getElementById(port.getIdElement()), port);
		}
		return null;
	}
	
	public Pair<Element, Port> getElementAndPortByPortIPAddress(String ip) {
		Port port = repository.getPortByIP(ip);
		if (port != null) {
			return new Pair<Element, Port>(repository.getElementById(port.getIdElement()), port);
		}
		return null;
	}

	public Port getPortByIP(String ip) {
		return repository.getPortByIP(ip);
	}

	public Set<Port> getPortsByRemoteIdElement(UUID idElement) {
		return repository.getPortsByRemoteIdElement(idElement);
	}
	
	public Port getPortByIdElementAndOfPort(UUID idElement, String ofPort) {
		return repository.getPortsByRemoteIdElement(idElement, ofPort);
	}
	
	public Set<Port> getLinkedPortsByIdElement(Set<UUID> idElementList) {
		Set<Port> resultSet = new HashSet<Port>();
		for(Port port : repository.getPorts()) {
			if(port.getRemoteIdPort() != null && idElementList.contains(port.getIdElement())) {
				resultSet.add(port);
			}
		}
		return resultSet;
	}

	public Set<Port> getPortsWithIP() {
		return repository.getPortsWithIP();
	}

	public List<Element> getElementsByType(ElementType type) {
		List<Element> resultSet = new ArrayList<Element>();
		for(Element element : repository.getElements()) {
			if(type.equals(element.getTypeElement())) {
				resultSet.add(element);
			}
		}
		return resultSet;
	}
}
