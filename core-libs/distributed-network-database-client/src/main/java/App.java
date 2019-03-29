import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import br.ufu.facom.mehar.sonar.core.database.client.repository.DatabaseBuilder;
import br.ufu.facom.mehar.sonar.core.database.client.repository.TopologyRepository;
import br.ufu.facom.mehar.sonar.core.database.client.repository.casandra.CassandraDatabaseBuilder;
import br.ufu.facom.mehar.sonar.core.database.client.repository.casandra.CassandraTopologyRepository;
import br.ufu.facom.mehar.sonar.core.database.client.service.TopologyService;
import br.ufu.facom.mehar.sonar.core.model.topology.Domain;
import br.ufu.facom.mehar.sonar.core.util.IPUtils;

public class App {
	public static void main(String[] args) {
		Domain domain = new Domain();
		domain.setName("Unique");
		domain.setNetworkAddress("192.168.0.0");
		domain.setNetworkMask("255.255.255.0");
		
//		DatabaseBuilder builder = new CassandraDatabaseBuilder();
//		builder.buildOrAlter();
		
//		TopologyRepository repo = new CassandraTopologyRepository();
		
		TopologyService service = new TopologyService();
		System.out.println(service.deleteDomains());
		
//		Domain result = service.save(domain);
//		System.out.println(ToStringBuilder.reflectionToString(result, ToStringStyle.MULTI_LINE_STYLE));
		
		
//		Domain result = repo.save(domain);
//		for(Domain d : repo.getDomains()) {
//			System.out.println(ToStringBuilder.reflectionToString(d, ToStringStyle.MULTI_LINE_STYLE));
//		}
		
//		System.out.println(IPUtils.isInNetwork("192.168.0.0", "192.168.0.0", "255.255.255.0"));
	}
}
