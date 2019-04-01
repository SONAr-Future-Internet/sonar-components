package br.ufu.facom.mehar.sonar.client.nddb.repository.casandra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.datastax.driver.core.Session;

import br.ufu.facom.mehar.sonar.client.nddb.exception.DatabaseBuilderException;
import br.ufu.facom.mehar.sonar.client.nddb.repository.DatabaseBuilder;

public class CassandraDatabaseBuilder extends CassandraGenericRepository implements DatabaseBuilder {

	@Override
	public void buildOrAlter() {
		Session session = session();
		try {
			List<String> schemaStatements = readSchemaFile();
			for(String statement : schemaStatements) {
				session.execute(statement);
			}
		} catch (IOException e) {
			throw new DatabaseBuilderException("Error opening file database/cassandra/schema.cql.", e);
		} finally {
			close(session);
		}
	}

	private List<String> readSchemaFile() throws IOException {
		Resource resource = new ClassPathResource("database/cassandra/schema.cql");
		BufferedReader buf = new BufferedReader(new InputStreamReader(resource.getInputStream()));
		
		List<String> statements = new ArrayList<String>();
		
		StringBuilder sb = new StringBuilder();
		
		String line = buf.readLine();
		while (line != null) {
			if(!line.trim().startsWith("--")) {
				sb.append(line).append("\n");
				if(line.trim().endsWith(";")) {
					statements.add(sb.toString());
					sb = new StringBuilder();
				}
			}
			line = buf.readLine();
		}
		return statements;
	}

}
