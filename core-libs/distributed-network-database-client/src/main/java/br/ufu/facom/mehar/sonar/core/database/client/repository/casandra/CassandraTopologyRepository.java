package br.ufu.facom.mehar.sonar.core.database.client.repository.casandra;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.utils.UUIDs;

import br.ufu.facom.mehar.sonar.core.database.client.repository.TopologyRepository;
import br.ufu.facom.mehar.sonar.core.model.topology.Domain;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;


@Repository
public class CassandraTopologyRepository extends CassandraGenericRepository implements TopologyRepository{

	@Override
	public Domain save(Domain domain) {
		Session session = session();
		try {
			domain.setIdDomain(UUIDs.timeBased());
			
			String cql = "INSERT INTO domain JSON '"+fromObject(domain)+"'";
			
		    session.execute(cql);
		    
		    return domain;
		}finally {
			close(session);
		}
	}

	@Override
	public Element save(Element element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Port save(Port element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Domain update(Domain domain) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element update(Element element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Port update(Port element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean delete(Domain domain) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean delete(Element element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean delete(Port port) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Domain getDomainById(Domain idDomain) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Domain getDomainByIPAddress(String ip) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element getElementById(Long idElement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element getElementByIPAddress(String address) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element getElementByHostname(String address) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element getElementByPortMacAddress(String macAddress) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Port getPortById(Long idPort) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Port getPortByMacAddress(String macAddress) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Port getByHostnameAndIfId(String hostname, String ifId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Port getByIpAddressAndIfId(String ip, String ifId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Port> getPortByElement(Element element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Port> getPortByIdElement(Long idElement) {
		// TODO Auto-generated method stub
		return null;
	}
//
//	@Override
//	public Domain save(Domain domain) {
//		Session session = session();
//		
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Element save(Element element) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Port save(Port element) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Domain update(Domain domain) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Element update(Element element) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Port update(Port element) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Boolean delete(Domain domain) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Boolean delete(Element element) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Boolean delete(Port port) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Domain getDomainById(Domain idDomain) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Domain getDomainByIPAddress(String ip) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Element getElementById(Long idElement) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Element getElementByIPAddress(String address) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Element getElementByHostname(String address) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Element getElementByPortMacAddress(String macAddress) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Port getPortById(Long idPort) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Port getPortByMacAddress(String macAddress) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Port getByHostnameAndIfId(String hostname, String ifId) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Port getByIpAddressAndIfId(String ip, String ifId) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<Port> getPortByElement(Element element) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<Port> getPortByIdElement(Long idElement) {
//		// TODO Auto-generated method stub
//		return null;
//	}

}
