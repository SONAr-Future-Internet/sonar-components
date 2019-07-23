package br.ufu.facom.mehar.sonar.client.ndb.repository.impl.casandra;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Truncate;
import com.datastax.driver.core.utils.UUIDs;

import br.ufu.facom.mehar.sonar.client.ndb.repository.CoreRepository;
import br.ufu.facom.mehar.sonar.core.model.core.Controller;
import br.ufu.facom.mehar.sonar.core.util.ObjectUtils;

@Repository
public class CassandraCoreRepository extends CassandraGenericRepository implements CoreRepository {
	private static final String KEYSPACE = "core";
	private static final String CONTROLLER_COLECTION = "controller";

	@Override
	public Controller save(Controller controller) {
		Session session = session();
		try {
			if (controller.getIdController() == null) {
				controller.setIdController(UUIDs.random());
			}

			Insert insertQuery = QueryBuilder.insertInto(KEYSPACE, CONTROLLER_COLECTION)
					.json(ObjectUtils.fromObject(controller));
			session.execute(insertQuery);

			return controller;
		} finally {
			close(session);
		}
	}

	@Override
	public List<Controller> getControllers() {
		Session session = session();
		try {
			Select select = QueryBuilder.select().json().from(KEYSPACE, CONTROLLER_COLECTION);
			ResultSet rs = session.execute(select);

			List<Controller> result = new ArrayList<Controller>();
			for (Row r : rs.all()) {
				result.add(ObjectUtils.toObject(r.getString(0), Controller.class));
			}

			return result;
		} finally {
			close(session);
		}
	}

	@Override
	public Controller getControllerById(UUID idController) {
		Session session = session();
		try {
			Select.Where select = QueryBuilder.select().json().from(KEYSPACE, CONTROLLER_COLECTION).allowFiltering()
					.where(QueryBuilder.eq("idController", idController));
			
			ResultSet rs = session.execute(select);

			for (Row r : rs.all()) {
				return (ObjectUtils.toObject(r.getString(0), Controller.class));
			}

			return null;
		} finally {
			close(session);
		}
	}

	@Override
	public List<Controller> getControllersByInterceptor(String interceptor) {
		Session session = session();
		try {
			Select.Where select = QueryBuilder.select().json().from(KEYSPACE, CONTROLLER_COLECTION).allowFiltering()
					.where(QueryBuilder.eq("interceptor", interceptor));
			
			ResultSet rs = session.execute(select);

			List<Controller> result = new ArrayList<Controller>();
			for (Row r : rs.all()) {
				result.add(ObjectUtils.toObject(r.getString(0), Controller.class));
			}

			return result;
		} finally {
			close(session);
		}
	}

	@Override
	public Boolean deleteControllers() {
		Session session = session();
		try {
			Truncate truncate = QueryBuilder.truncate(KEYSPACE, CONTROLLER_COLECTION);
			
			session.execute(truncate);

			return Boolean.TRUE;
		} finally {
			close(session);
		}
	}
}
