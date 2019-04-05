package br.ufu.facom.mehar.sonar.client.dndb.repository.impl.casandra;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;

import br.ufu.facom.mehar.sonar.client.dndb.repository.PropertyRepository;
import br.ufu.facom.mehar.sonar.core.model.property.ConfigurationProperty;
import br.ufu.facom.mehar.sonar.core.model.property.DataProperty;
import br.ufu.facom.mehar.sonar.core.util.ObjectUtils;

@Repository
public class CassandraPropertyRepository extends CassandraGenericRepository implements PropertyRepository {
	private static final String KEYSPACE = "property";
	private static final String CONFIGURATION_COLECTION = "configuration";
	private static final String DATA_COLECTION = "data";

	@Override
	public List<ConfigurationProperty> getConfiguration() {
		Session session = session(KEYSPACE);
		try {
			Select select = QueryBuilder.select().json().from(KEYSPACE, CONFIGURATION_COLECTION);
			ResultSet rs = session.execute(select);

			List<ConfigurationProperty> result = new ArrayList<ConfigurationProperty>();
			for (Row r : rs.all()) {
				result.add(ObjectUtils.toObject(r.getString(0), ConfigurationProperty.class));
			}

			return result;
		} finally {
			close(session);
		}
	}

	@Override
	public List<ConfigurationProperty> getConfiguration(String group) {
		Session session = session(KEYSPACE);
		try {
			Select.Where select = QueryBuilder.select().json().from(KEYSPACE, CONFIGURATION_COLECTION)
					.where(QueryBuilder.eq("group", group));
			ResultSet rs = session.execute(select);

			List<ConfigurationProperty> result = new ArrayList<ConfigurationProperty>();
			for (Row r : rs.all()) {
				result.add(ObjectUtils.toObject(r.getString(0), ConfigurationProperty.class));
			}

			return result;
		} finally {
			close(session);
		}
	}

	@Override
	public ConfigurationProperty getConfiguration(String group, String key) {
		Session session = session(KEYSPACE);
		try {
			Select.Where select = QueryBuilder.select().json().from(KEYSPACE, CONFIGURATION_COLECTION)
					.where(QueryBuilder.eq("group", group)).and(QueryBuilder.eq("key", key));
			ResultSet rs = session.execute(select);

			return ObjectUtils.toObject(rs.one().getString(0), ConfigurationProperty.class);

		} finally {
			close(session);
		}
	}

	@Override
	public Boolean setProperty(ConfigurationProperty property) {
		Session session = session(KEYSPACE);
		try {

			Insert insertQuery = QueryBuilder.insertInto(KEYSPACE, CONFIGURATION_COLECTION).json(ObjectUtils.fromObject(property));

			session.execute(insertQuery);

			return Boolean.TRUE;
		} finally {
			close(session);
		}
	}

	@Override
	public Boolean setProperty(String group, String key, String value) {
		return this.setProperty(new ConfigurationProperty(group, key, value));
	}

	@Override
	public List<DataProperty> getData() {
		Session session = session(KEYSPACE);
		try {
			Select select = QueryBuilder.select().json().from(KEYSPACE, DATA_COLECTION);
			ResultSet rs = session.execute(select);

			List<DataProperty> result = new ArrayList<DataProperty>();
			for (Row r : rs.all()) {
				result.add(ObjectUtils.toObject(r.getString(0), DataProperty.class));
			}

			return result;
		} finally {
			close(session);
		}
	}

	@Override
	public List<DataProperty> getData(String application) {
		Session session = session(KEYSPACE);
		try {
			Select.Where select = QueryBuilder.select().json().from(KEYSPACE, DATA_COLECTION)
					.where(QueryBuilder.eq("application", application));
			ResultSet rs = session.execute(select);

			List<DataProperty> result = new ArrayList<DataProperty>();
			for (Row r : rs.all()) {
				result.add(ObjectUtils.toObject(r.getString(0), DataProperty.class));
			}

			return result;
		} finally {
			close(session);
		}
	}

	@Override
	public List<DataProperty> getData(String application, String instance) {
		Session session = session(KEYSPACE);
		try {
			Select.Where select = QueryBuilder.select().json().from(KEYSPACE, DATA_COLECTION)
					.where(QueryBuilder.eq("application", application)).and(QueryBuilder.eq("instance", instance));
			ResultSet rs = session.execute(select);

			List<DataProperty> result = new ArrayList<DataProperty>();
			for (Row r : rs.all()) {
				result.add(ObjectUtils.toObject(r.getString(0), DataProperty.class));
			}

			return result;
		} finally {
			close(session);
		}
	}

	@Override
	public List<DataProperty> getData(String application, String instance, String group) {
		Session session = session(KEYSPACE);
		try {
			Select.Where select = QueryBuilder.select().json().from(KEYSPACE, DATA_COLECTION)
					.where(QueryBuilder.eq("application", application)).and(QueryBuilder.eq("instance", instance))
					.and(QueryBuilder.eq("group", group));
			ResultSet rs = session.execute(select);

			List<DataProperty> result = new ArrayList<DataProperty>();
			for (Row r : rs.all()) {
				result.add(ObjectUtils.toObject(r.getString(0), DataProperty.class));
			}

			return result;
		} finally {
			close(session);
		}
	}

	@Override
	public DataProperty getData(String application, String instance, String group, String key) {
		Session session = session(KEYSPACE);
		try {
			Select.Where select = QueryBuilder.select().json().from(KEYSPACE, DATA_COLECTION)
					.where(QueryBuilder.eq("application", application)).and(QueryBuilder.eq("instance", instance))
					.and(QueryBuilder.eq("group", group)).and(QueryBuilder.eq("key", key));
			ResultSet rs = session.execute(select);

			return ObjectUtils.toObject(rs.one().getString(0), DataProperty.class);
		} finally {
			close(session);
		}
	}

	@Override
	public Boolean setData(DataProperty property) {
		Session session = session(KEYSPACE);
		try {

			Insert insertQuery = QueryBuilder.insertInto(KEYSPACE, DATA_COLECTION).json(ObjectUtils.fromObject(property));

			session.execute(insertQuery);

			return Boolean.TRUE;
		} finally {
			close(session);
		}
	}

	@Override
	public Boolean setData(String application, String instance, String group, String key, String value) {
		return this.setData(new DataProperty(application, instance, group, key, value));
	}

}
