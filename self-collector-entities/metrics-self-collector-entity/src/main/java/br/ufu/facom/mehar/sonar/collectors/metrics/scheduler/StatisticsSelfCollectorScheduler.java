package br.ufu.facom.mehar.sonar.collectors.metrics.scheduler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import br.ufu.facom.mehar.sonar.client.nem.configuration.SonarTopics;
import br.ufu.facom.mehar.sonar.client.nem.service.EventService;
//import br.ufu.facom.mehar.sonar.collectors.metrics.amqp.MessageSender;
import br.ufu.facom.mehar.sonar.collectors.metrics.configuration.AppProperties;
import br.ufu.facom.mehar.sonar.collectors.metrics.model.Statistics;
import br.ufu.facom.mehar.sonar.collectors.metrics.network.topology.DynamicTopology;

@Component
public class StatisticsSelfCollectorScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsSelfCollectorScheduler.class);
	@Autowired
	private AppProperties appProperties;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private DynamicTopology dynamicTopology;
	// @Autowired
	// private MessageSender messageSender;
	@Autowired
	private EventService publisher;
	private List<String> devices;

	@PostConstruct
	private void init() {
		devices = dynamicTopology.getDevices();
	}

	@Scheduled(fixedDelayString = "${statistics.self.collector.scheduler.fixed.delay}")
	public void run() throws URISyntaxException {
		URI onosURI;
		Statistics statistics;
		for (String device : devices) {
			try {
				onosURI = new URI("http", null, appProperties.getOnosIp(), appProperties.getOnosPort(),
						appProperties.getOnosApiStatisticsPortsPath() + device, null, null);
				LOGGER.debug(onosURI.toString());
				statistics = restTemplate.getForObject(onosURI, Statistics.class);
				LOGGER.debug("{}", statistics);
				postToElasticIndex(statistics);
				publishToTopic(statistics);
			} catch (URISyntaxException e) {
				LOGGER.error(e.getMessage());
			}
		}
	}

	private void postToElasticIndex(Statistics statistics) {
		URI elasticURI;
		try {
			elasticURI = new URI("http", null, appProperties.getElasticIp(), appProperties.getElasticPort(),
					appProperties.getElasticApiIndexPath(), null, null);
			HttpEntity<Statistics> request = new HttpEntity<>(statistics);
			restTemplate.postForEntity(elasticURI, request, Statistics.class);
		} catch (URISyntaxException e) {
			LOGGER.error(e.getMessage());
		}
	}

	private void publishToTopic(Statistics statistics) {
		publisher.publish(SonarTopics.TOPIC_METRICS, statistics);
	}

}
