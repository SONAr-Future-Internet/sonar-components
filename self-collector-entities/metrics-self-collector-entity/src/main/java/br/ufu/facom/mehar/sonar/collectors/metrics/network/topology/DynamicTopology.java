package br.ufu.facom.mehar.sonar.collectors.metrics.network.topology;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import br.ufu.facom.mehar.sonar.collectors.metrics.configuration.AppProperties;
import br.ufu.facom.mehar.sonar.collectors.metrics.model.Cluster;
import br.ufu.facom.mehar.sonar.collectors.metrics.model.ClusterDevices;
import br.ufu.facom.mehar.sonar.collectors.metrics.model.Topology;

@Service
public class DynamicTopology {

	private static final Logger LOGGER = LoggerFactory.getLogger(DynamicTopology.class);
	@Autowired
	private AppProperties appProperties;
	@Autowired
	private RestTemplate restTemplate;
	
	public List<String> getDevices() {
		URI onosURI;
		List<String> devices = new ArrayList<String>();
		try {
			onosURI = new URI("http", null, appProperties.getOnosIp(), appProperties.getOnosPort(),
					appProperties.getOnosApiTopologyClustersPath(), null, null);
			LOGGER.debug(onosURI.toString());
			Topology topology = restTemplate.getForObject(onosURI, Topology.class);
			for (Cluster cluster: topology.getClusters()) {
				String path = appProperties.getOnosApiTopologyClustersDevicesPath().replace("{clusterId}", cluster.getId().toString());
				onosURI = new URI("http", null, appProperties.getOnosIp(), appProperties.getOnosPort(),
						path, null, null);
				LOGGER.debug(onosURI.toString());
				ClusterDevices clusterDevices = restTemplate.getForObject(onosURI, ClusterDevices.class);
				LOGGER.debug(clusterDevices.toString());
				devices.addAll(clusterDevices.getDevices());
			}
			LOGGER.debug(topology.toString());
		} catch (URISyntaxException e) {
			LOGGER.error(e.getMessage());
		}
		return devices;
	}
	
}
