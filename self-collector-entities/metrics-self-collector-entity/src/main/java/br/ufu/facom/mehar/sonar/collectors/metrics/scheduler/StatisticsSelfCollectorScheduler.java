package br.ufu.facom.mehar.sonar.collectors.metrics.scheduler;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import br.ufu.facom.mehar.sonar.collectors.metrics.configuration.AppProperties;
import br.ufu.facom.mehar.sonar.collectors.metrics.model.Statistics;

@Component
public class StatisticsSelfCollectorScheduler {

	private final Logger logger = LoggerFactory.getLogger(StatisticsSelfCollectorScheduler.class);

	@Autowired
	private AppProperties appProperties;
	@Autowired
	private RestTemplate restTemplate;
	private Statistics statistics;

	@Scheduled(fixedDelayString = "${statistics.collector.scheduler.fixed.delay}")
	public void run() throws URISyntaxException {
		URI onosURI;
		for (int i = 0; i < appProperties.getDevicesList().length; i++) {
			try {
				onosURI = new URI("http://" + appProperties.getOnosIp() + ":" + appProperties.getOnosPort()
						+ appProperties.getStatisticsBaseEndpoint() + appProperties.getDevicesList()[i]);
				logger.info(onosURI.toString());
				restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor(appProperties.getOnosUser(),
						appProperties.getOnosPassword()));
				statistics = restTemplate.getForObject(onosURI, Statistics.class);
				logger.info("{}", statistics);
				postToElasticIndex(statistics);
			} catch (URISyntaxException e) {
				logger.error(e.getMessage());
			}
		}
	}

	private void postToElasticIndex(Statistics statistics) {
		URI elasticURI;
		try {
			elasticURI = new URI("http://" + appProperties.getElasticIp() + ":" + appProperties.getElasticPort()
					+ appProperties.getElasticEndpoint());
			HttpEntity<Statistics> request = new HttpEntity<>(statistics);
			restTemplate.postForEntity(elasticURI, request, Statistics.class);
		} catch (URISyntaxException e) {
			logger.error(e.getMessage());
		}
	}

}
