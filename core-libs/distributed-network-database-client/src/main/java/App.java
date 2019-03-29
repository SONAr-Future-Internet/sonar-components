import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import br.ufu.facom.mehar.sonar.core.database.client.repository.TopologyRepository;
import br.ufu.facom.mehar.sonar.core.database.client.repository.casandra.CassandraTopologyRepository;
import br.ufu.facom.mehar.sonar.core.model.topology.Domain;

public class App {
	public static void main(String[] args) {
		Domain domain = new Domain();
		domain.setName("Unique");
		domain.setNetworkAddress("192.168.0.0");
		domain.setNetworkMask("255.255.255.255");
		
		TopologyRepository repo = new CassandraTopologyRepository();
		
		Domain result = repo.save(domain);
		
		System.out.println(ToStringBuilder.reflectionToString(result, ToStringStyle.MULTI_LINE_STYLE));
	}
}
