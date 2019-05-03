package br.ufu.facom.mehar.sonar.collectors.metrics.scheduler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.vmware.ovsdb.protocol.operation.Operation;
import com.vmware.ovsdb.protocol.operation.Select;
import com.vmware.ovsdb.protocol.operation.notation.Row;
import com.vmware.ovsdb.protocol.operation.result.OperationResult;
import com.vmware.ovsdb.protocol.operation.result.SelectResult;
import com.vmware.ovsdb.service.OvsdbActiveConnectionConnector;
import com.vmware.ovsdb.service.OvsdbClient;
import com.vmware.ovsdb.service.impl.OvsdbActiveConnectionConnectorImpl;

import br.ufu.facom.mehar.sonar.client.nem.configuration.SonarTopics;
import br.ufu.facom.mehar.sonar.client.nem.service.EventService;
import br.ufu.facom.mehar.sonar.collectors.metrics.configuration.AppProperties;
import br.ufu.facom.mehar.sonar.collectors.metrics.extractor.SelectResultRowExtractor;
import br.ufu.facom.mehar.sonar.collectors.metrics.model.Interface;

@Component
public class StatisticsSelfCollectorScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsSelfCollectorScheduler.class);
	private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
	@Autowired
	private AppProperties appProperties;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private EventService publisher;
	@Value("${ovsdb.completable.future.timeout}")
	private Integer cfTimeout;
	@Value("${ovsdb.port}")
	private Integer ovsdbPort;
	@Value("${ovsdb.ips}")
	private String[] ovsdbIps;
	private List<OvsdbClient> ovsdbClients;

	@PostConstruct
	private void init() {
		OvsdbActiveConnectionConnector ovsdbActiveConnectionConnector;
		ovsdbClients = new ArrayList<OvsdbClient>();
		ovsdbActiveConnectionConnector = new OvsdbActiveConnectionConnectorImpl(scheduledExecutorService);
		for (String ip : ovsdbIps) {
			ovsdbClients.add(ovsdbActiveConnectionConnector.connect(ip, ovsdbPort).join());
		}
	}

	@Scheduled(fixedDelayString = "${statistics.self.collector.scheduler.fixed.delay}")
	public void run() {
		try {
			SelectResultRowExtractor selectResultRowExtractor;
			String[] tableColumns = appProperties.getOvsdbTableColumns();
			String tableName = appProperties.getOvsdbTableName();
			String databaseName = appProperties.getOvsdbDatabaseName();
			for (OvsdbClient ovsdbClient : ovsdbClients) {
				CompletableFuture<String[]> cfListDatabases = ovsdbClient.listDatabases();
				String[] databases = cfListDatabases.get(cfTimeout, TimeUnit.SECONDS);
				if (Arrays.asList(databases).contains(appProperties.getOvsdbDatabaseName())) {
					List<Operation> operations = new ArrayList<Operation>();
					Operation operation = new Select(tableName).columns(tableColumns);
					operations.add(operation);
					CompletableFuture<OperationResult[]> cfOperationResults = ovsdbClient.transact(databaseName,
							operations);
					OperationResult[] operationResults = cfOperationResults.get(cfTimeout, TimeUnit.SECONDS);
					for (OperationResult operationResult : operationResults) {
						SelectResult selectResult = (SelectResult) operationResult;
						List<Row> rows = selectResult.getRows();
						for (Row row : rows) {
							selectResultRowExtractor = new SelectResultRowExtractor(row);
							Interface portInterface = selectResultRowExtractor.getInterface();
							postToElasticIndex(portInterface);
							publishToTopic(portInterface);
							LOGGER.info("{}", portInterface);
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("{}", e);
		}
	}

	private void postToElasticIndex(Interface portInterface) {
		URI elasticURI;
		try {
			elasticURI = new URI("http", null, appProperties.getElasticIp(), appProperties.getElasticPort(),
					appProperties.getElasticApiIndexPath(), null, null);
			HttpEntity<Interface> request = new HttpEntity<>(portInterface);
			restTemplate.postForEntity(elasticURI, request, Interface.class);
		} catch (URISyntaxException e) {
			LOGGER.error(e.getMessage());
		}
	}

	private void publishToTopic(Interface portInterface) {
		publisher.publish(SonarTopics.TOPIC_METRICS, portInterface);
	}

}
