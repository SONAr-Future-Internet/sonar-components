package br.ufu.facom.mehar.sonar.client.ndb.repository.impl.casandra;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Truncate;
import com.datastax.driver.core.utils.UUIDs;

import br.ufu.facom.mehar.sonar.client.ndb.repository.TopologyRepository;
import br.ufu.facom.mehar.sonar.core.model.topology.Domain;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;
import br.ufu.facom.mehar.sonar.core.util.ObjectUtils;

@Repository
public class CassandraTopologyRepository extends CassandraGenericRepository implements TopologyRepository {
	private static final String KEYSPACE = "topology";
	private static final String DOMAIN_COLECTION = "domain";
	private static final String ELEMENT_COLECTION = "element";
	private static final String PORT_COLECTION = "port";

	@Override
	public Domain save(Domain domain) {
		Session session = session();
		try {
			if (domain.getIdDomain() == null) {
				domain.setIdDomain(UUIDs.random());
			}

			Insert insertQuery = QueryBuilder.insertInto(KEYSPACE, DOMAIN_COLECTION)
					.json(ObjectUtils.fromObject(domain, "elementList"));
			session.execute(insertQuery);

			return domain;
		} finally {
			close(session);
		}
	}

	@Override
	public Element save(Element element) {
		Session session = session();
		try {
			if (element.getIdElement() == null) {
				element.setIdElement(UUIDs.random());
			}

			Insert insertQuery = QueryBuilder.insertInto(KEYSPACE, ELEMENT_COLECTION)
					.json(ObjectUtils.fromObject(element, "domain", "portList"));
			session.execute(insertQuery);

			return element;
		} finally {
			close(session);
		}
	}
	

	@Override
	public Port save(Port port) {
		Session session = session();
		try {
			if (port.getIdPort() == null) {
				port.setIdPort(UUIDs.random());
			}

			Insert insertQuery = QueryBuilder.insertInto(KEYSPACE, PORT_COLECTION)
					.json(ObjectUtils.fromObject(port, "element", "remotePort"));
			session.execute(insertQuery);

			return port;
		} finally {
			close(session);
		}
	}

	@Override
	public Domain update(Domain domain) {
		Session session = session();
		try {
			this.delete(domain);
			return this.save(domain);
		} finally {
			close(session);
		}
	}

	@Override
	public Element update(Element element) {
		Session session = session();
		try {
			this.delete(element);
			return this.save(element);
		} finally {
			close(session);
		}
	}

	@Override
	public Port update(Port port) {
		Session session = session();
		try {
			this.delete(port);
			return this.save(port);
		} finally {
			close(session);
		}
	}

	@Override
	public Boolean delete(Domain domain) {
		Session session = session();
		try {
			Delete.Where delete = QueryBuilder.delete().all().from(KEYSPACE, DOMAIN_COLECTION)
					.where(QueryBuilder.eq("idDomain", domain.getIdDomain()));
			session.execute(delete);

			return Boolean.TRUE;
		} finally {
			close(session);
		}
	}

	@Override
	public Boolean delete(Element element) {
		Session session = session();
		try {
			Delete.Where delete = QueryBuilder.delete().all().from(KEYSPACE, ELEMENT_COLECTION)
					.where(QueryBuilder.eq("idElement", element.getIdElement()));
			session.execute(delete);

			return Boolean.TRUE;
		} finally {
			close(session);
		}
	}

	@Override
	public Boolean delete(Port port) {
		Session session = session();
		try {
			Delete.Where delete = QueryBuilder.delete().all().from(KEYSPACE, PORT_COLECTION)
					.where(QueryBuilder.eq("idPort", port.getIdPort()));
			
			session.execute(delete);

			return Boolean.TRUE;
		} finally {
			close(session);
		}
	}
	
	@Override
	public Boolean deleteDomains() {
		Session session = session();
		try {
			Truncate truncate = QueryBuilder.truncate(KEYSPACE, DOMAIN_COLECTION);
			
			session.execute(truncate);

			return Boolean.TRUE;
		} finally {
			close(session);
		}
	}

	@Override
	public Boolean deleteElements() {
		Session session = session();
		try {
			Truncate truncate = QueryBuilder.truncate(KEYSPACE, ELEMENT_COLECTION);
			
			session.execute(truncate);

			return Boolean.TRUE;
		} finally {
			close(session);
		}
	}
	
	@Override
	public Boolean deletePorts() {
		Session session = session();
		try {
			Truncate truncate = QueryBuilder.truncate(KEYSPACE, PORT_COLECTION);
			
			session.execute(truncate);

			return Boolean.TRUE;
		} finally {
			close(session);
		}
	}
	
	@Override
	public Boolean deleteDomainById(UUID idDomain) {
		Session session = session();
		try {
			Delete.Where delete = QueryBuilder.delete().all().from(KEYSPACE, DOMAIN_COLECTION)
					.where(QueryBuilder.eq("idDomain", idDomain));
			
			session.execute(delete);

			return Boolean.TRUE;
		} finally {
			close(session);
		}
	}

	@Override
	public Boolean deleteElementById(UUID idElement) {
		Session session = session();
		try {
			Delete.Where delete = QueryBuilder.delete().all().from(KEYSPACE, ELEMENT_COLECTION)
					.where(QueryBuilder.eq("idElement", idElement));
			
			session.execute(delete);

			return Boolean.TRUE;
		} finally {
			close(session);
		}
	}

	@Override
	public Boolean deletePortById(UUID idPort) {
		Session session = session();
		try {
			Delete.Where delete = QueryBuilder.delete().all().from(KEYSPACE, PORT_COLECTION)
					.where(QueryBuilder.eq("idPort", idPort));
			
			session.execute(delete);

			return Boolean.TRUE;
		} finally {
			close(session);
		}
	}

	@Override
	public Boolean deleteElementByIdDomain(UUID idDomain) {
		Session session = session();
		try {
			Delete.Where delete = QueryBuilder.delete().all().from(KEYSPACE, ELEMENT_COLECTION)
					.where(QueryBuilder.eq("idDomain", idDomain));
			
			session.execute(delete);

			return Boolean.TRUE;
		} finally {
			close(session);
		}
	}

	@Override
	public Boolean deletePortByIdElement(UUID idElement) {
		Session session = session();
		try {
			Select.Where select = QueryBuilder.select().json().from(KEYSPACE, PORT_COLECTION).allowFiltering()
					.where(QueryBuilder.eq("idElement", idElement));;
			ResultSet rs = session.execute(select);
			
			for(Row r : rs.all()) {
				Delete.Where delete = QueryBuilder.delete().all().from(KEYSPACE, PORT_COLECTION)
						.where(QueryBuilder.eq("idPort", ObjectUtils.toObject(r.getString(0), Port.class).getIdPort()) );
				
				session.execute(delete);
			}

			return Boolean.TRUE;
		} finally {
			close(session);
		}
	}

	@Override
	public List<Domain> getDomains() {
		Session session = session();
		try {
			Select select = QueryBuilder.select().json().from(KEYSPACE, DOMAIN_COLECTION);
			ResultSet rs = session.execute(select);
			
			List<Domain> result = new ArrayList<Domain>();
			for(Row r : rs.all()) {
				result.add(ObjectUtils.toObject(r.getString(0), Domain.class));
			}
			
			return result;
		} finally {
			close(session);
		}
	}

	@Override
	public Domain getDomainById(Domain idDomain) {
		Session session = session();
		try {
			Select.Where select = QueryBuilder.select().json().from(KEYSPACE, DOMAIN_COLECTION).allowFiltering()
					.where(QueryBuilder.eq("idDomain", idDomain));;
			ResultSet rs = session.execute(select);
			
			for(Row r : rs.all()) {
				return(ObjectUtils.toObject(r.getString(0), Domain.class));
			}
			
			return null;
		} finally {
			close(session);
		}
	}

	@Override
	public List<Element> getElements() {
		Session session = session();
		try {
			Select select = QueryBuilder.select().json().from(KEYSPACE, ELEMENT_COLECTION);
			ResultSet rs = session.execute(select);
			
			List<Element> result = new ArrayList<Element>();
			for(Row r : rs.all()) {
				result.add(ObjectUtils.toObject(r.getString(0), Element.class));
			}
			
			return result;
		} finally {
			close(session);
		}
	}

	@Override
	public List<Element> getElementsByIdDomain(UUID idDomain) {
		Session session = session();
		try {
			Select.Where select = QueryBuilder.select().json().from(KEYSPACE, ELEMENT_COLECTION).allowFiltering()
					.where(QueryBuilder.eq("idDomain", idDomain));;
			ResultSet rs = session.execute(select);
			
			List<Element> result = new ArrayList<Element>();
			for(Row r : rs.all()) {
				result.add(ObjectUtils.toObject(r.getString(0), Element.class));
			}
			
			return result;
		} finally {
			close(session);
		}
	}

	@Override
	public Element getElementById(UUID idElement) {
		Session session = session();
		try {
			Select.Where select = QueryBuilder.select().json().from(KEYSPACE, ELEMENT_COLECTION).allowFiltering()
					.where(QueryBuilder.eq("idElement", idElement));;
			ResultSet rs = session.execute(select);
			
			for(Row r : rs.all()) {
				return(ObjectUtils.toObject(r.getString(0), Element.class));
			}
			
			return null;
		} finally {
			close(session);
		}
	}

	@Override
	public Element getElementByIPAddress(String address) {
		Session session = session();
		try {
			Select.Where select = QueryBuilder.select().json().from(KEYSPACE, ELEMENT_COLECTION).allowFiltering()
					.where(QueryBuilder.contains("ipAddressList", address));;
			ResultSet rs = session.execute(select);
			
			for(Row r : rs.all()) {
				return(ObjectUtils.toObject(r.getString(0), Element.class));
			}
			
			return null;
		} finally {
			close(session);
		}
	}

	@Override
	public List<Element> getElementsByHostname(String name) {
		Session session = session();
		try {
			Select.Where select = QueryBuilder.select().json().from(KEYSPACE, ELEMENT_COLECTION).allowFiltering()
					.where(QueryBuilder.contains("name", name));;
			ResultSet rs = session.execute(select);
			
			List<Element> result = new ArrayList<Element>();
			for(Row r : rs.all()) {
				result.add(ObjectUtils.toObject(r.getString(0), Element.class));
			}
			
			return result;
		} finally {
			close(session);
		}
	}
	
	@Override
	public Set<Port> getPorts() {
		Session session = session();
		try {
			Select select = QueryBuilder.select().json().from(KEYSPACE, PORT_COLECTION);
			ResultSet rs = session.execute(select);
			
			Set<Port> result = new HashSet<Port>();
			for(Row r : rs.all()) {
				result.add(ObjectUtils.toObject(r.getString(0), Port.class));
			}
			
			return result;
		} finally {
			close(session);
		}
	}

	@Override
	public Set<Port> getPortsByIdElement(UUID idElement) {
		Session session = session();
		try {
			Select.Where select = QueryBuilder.select().json().from(KEYSPACE, PORT_COLECTION).allowFiltering()
					.where(QueryBuilder.eq("idElement", idElement));;
			ResultSet rs = session.execute(select);
			
			Set<Port> result = new HashSet<Port>();
			for(Row r : rs.all()) {
				result.add(ObjectUtils.toObject(r.getString(0), Port.class));
			}
			
			return result;
		} finally {
			close(session);
		}
	}
	
	@Override
	public Port getPortsByRemoteIdElement(UUID idElement, String ofPort) {
		Session session = session();
		try {
			Select.Where select = QueryBuilder.select().json().from(KEYSPACE, PORT_COLECTION).allowFiltering()
					.where(QueryBuilder.eq("idElement", idElement)).and(QueryBuilder.eq("ofPort", ofPort));;
			ResultSet rs = session.execute(select);
			
			for(Row r : rs.all()) {
				return ObjectUtils.toObject(r.getString(0), Port.class);
			}
			
			return null;
		} finally {
			close(session);
		}
	}

	@Override
	public Port getPortById(UUID idPort) {
		Session session = session();
		try {
			Select.Where select = QueryBuilder.select().json().from(KEYSPACE, PORT_COLECTION).allowFiltering()
					.where(QueryBuilder.eq("idPort", idPort));;
			ResultSet rs = session.execute(select);
			
			for(Row r : rs.all()) {
				return(ObjectUtils.toObject(r.getString(0), Port.class));
			}
			
			return null;
		} finally {
			close(session);
		}
	}

	@Override
	public Port getPortByMacAddress(String macAddress) {
		Session session = session();
		try {
			Select.Where select = QueryBuilder.select().json().from(KEYSPACE, PORT_COLECTION).allowFiltering()
					.where(QueryBuilder.eq("macAddress", macAddress));;
			ResultSet rs = session.execute(select);
			
			for(Row r : rs.all()) {
				return(ObjectUtils.toObject(r.getString(0), Port.class));
			}
			
			return null;
		} finally {
			close(session);
		}
	}

	@Override
	public Set<Port> getPortsWithIP() {
		Session session = session();
		try {
			Select.Where select = QueryBuilder.select().json().from(KEYSPACE, PORT_COLECTION).allowFiltering()
					.where(QueryBuilder.gt("ipAddress", ""));
			
			
			ResultSet rs = session.execute(select);
			
			Set<Port> result = new HashSet<Port>();
			for(Row r : rs.all()) {
				result.add(ObjectUtils.toObject(r.getString(0), Port.class));
			}
			
			return result;
		} finally {
			close(session);
		}
	}

	@Override
	public Port getPortByIP(String ip) {
		Session session = session();
		try {
			Select.Where select = QueryBuilder.select().json().from(KEYSPACE, PORT_COLECTION).allowFiltering()
					.where(QueryBuilder.eq("ipAddress", ip));;
			ResultSet rs = session.execute(select);
			
			for(Row r : rs.all()) {
				return ObjectUtils.toObject(r.getString(0), Port.class);
			}
			
			return null;
		} finally {
			close(session);
		}
	}

	@Override
	public Set<Port> getPortsByRemoteIdElement(UUID idElement) {
		Session session = session();
		try {
			//Select Ports
			Select.Where selectPorts = QueryBuilder.select().json().from(KEYSPACE, PORT_COLECTION).allowFiltering()
					.where(QueryBuilder.eq("idElement", idElement));
			
			ResultSet rs = session.execute(selectPorts);
			
			Set<UUID> ports = new HashSet<UUID>();
			for(Row r : rs.all()) {
				ports.add(ObjectUtils.toObject(r.getString(0), Port.class).getIdPort());
			}
			
			
			//Sellect Remote Ports
			Select.Where select = QueryBuilder.select().json().from(KEYSPACE, PORT_COLECTION).allowFiltering()
					.where(QueryBuilder.in("remoteIdPort", ports));;
			
			rs = session.execute(select);
					
			Set<Port> result = new HashSet<Port>();
			for(Row r : rs.all()) {
				result.add(ObjectUtils.toObject(r.getString(0), Port.class));
			}
			
			return result;
			
		} finally {
			close(session);
		}
	}
}
