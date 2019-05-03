package br.ufu.facom.mehar.sonar.client.nim.element.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.vmware.ovsdb.exception.OvsdbClientException;
import com.vmware.ovsdb.protocol.operation.Operation;
import com.vmware.ovsdb.protocol.operation.Select;
import com.vmware.ovsdb.protocol.operation.notation.Row;
import com.vmware.ovsdb.protocol.operation.result.OperationResult;
import com.vmware.ovsdb.protocol.operation.result.SelectResult;
import com.vmware.ovsdb.protocol.schema.DatabaseSchema;
import com.vmware.ovsdb.service.OvsdbActiveConnectionConnector;
import com.vmware.ovsdb.service.OvsdbClient;
import com.vmware.ovsdb.service.impl.OvsdbActiveConnectionConnectorImpl;

@Component("ovsdb")
public class OVSDBManager {
	private static final Logger logger = LoggerFactory.getLogger(OVSDBManager.class);

	/*
	 * Schema Constants: ${TABLE}_${COLUMN}
	 */
	private static final String CONTROLLER = "Controller";
	private static final String CONTROLLER_TARGET = "target";
	private static final String CONTROLLER_IS_CONNECTED = "is_connected";
	private static final String BRIDGE = "Bridge";
	private static final String BRIDGE_NAME = "name";
	private static final String BRIDGE_PORTS = "ports";
	private static final String FLOW_TABLE = "Flow_Table";

	private static final Integer TIMEOUT_IN_SECS = 3;

	private static final Integer DEFAULT_PORT = 6640;

	private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	private OvsdbActiveConnectionConnector connector = new OvsdbActiveConnectionConnectorImpl(executorService);

	public static void main(String[] args) {
		OVSDBManager ovsdbManager = new OVSDBManager();
		try {
//			ovsdbManager.configure("192.168.0.7");

			System.out.println("\n\n\n");
			System.out.println(ovsdbManager.hasController("192.168.0.7", "192.168.0.1:6633"));
			System.out.println("\n\n\n");
			System.out.println(ovsdbManager.hasBridge("192.168.0.7", "br0", Boolean.TRUE));
			System.out.println("\n\n\n");
		} finally {
			ovsdbManager.finish();
		}
	}

	public Boolean hasController(String ipDevice, String controllerTarget) {
		OvsdbClient client = connect(ipDevice, DEFAULT_PORT);
		try {
			SelectResult result = select(client, CONTROLLER);
			if (result != null && result.getRows() != null && !result.getRows().isEmpty()) {
				for (Row r : result.getRows()) {
					if (controllerTarget.equals(r.getStringColumn(CONTROLLER_TARGET))) {
						if (r.getBooleanColumn(CONTROLLER_IS_CONNECTED)) {
							return Boolean.TRUE;
						}
					}
				}
			}
			return Boolean.FALSE;
		} finally {
			close(client);
		}
	}

	public Boolean hasBridge(String ipDevice, String bridge, Boolean withAllPorts) {
		OvsdbClient client = connect(ipDevice, DEFAULT_PORT);
		try {
			SelectResult result = select(client, BRIDGE);
			if (result != null && result.getRows() != null && !result.getRows().isEmpty()) {
				for (Row r : result.getRows()) {
					if (bridge.equals(r.getStringColumn(BRIDGE_NAME))) {
						for (Object port : r.getSetColumn(BRIDGE_PORTS)) {
							System.out.println(port);
						}
					}
				}
			}
			return Boolean.FALSE;
		} finally {
			close(client);
		}
	}

	public void configure(String ip) {
		OvsdbClient client = connect(ip, DEFAULT_PORT);
		try {
			printSchema(client);
			select(client, BRIDGE);
		} finally {
			close(client);
		}
	}

	private SelectResult select(OvsdbClient client, String table) {
		try {
			List<Operation> command = new ArrayList<Operation>(Arrays.asList(new Select(table)));
			CompletableFuture<OperationResult[]> resultFuture = client.transact("Open_vSwitch", command);
			OperationResult[] resultList = resultFuture.get(3, TimeUnit.SECONDS);
			if (resultList != null && resultList.length > 0) {
				SelectResult selectResult = (SelectResult) resultList[0];
				logger.debug("'Select " + table + "' :" + selectResult.toString());
				return selectResult;
			}
			return null;
		} catch (OvsdbClientException | InterruptedException | ExecutionException | TimeoutException e) {
			logger.error("Error while executing 'Select " + table + "'", e);
			return null;
		}
	}

	private DatabaseSchema[] printSchema(OvsdbClient client) {

		try {
			CompletableFuture<String[]> f = client.listDatabases();
			String[] dbs = f.get(3, TimeUnit.SECONDS);

			DatabaseSchema[] schemas = new DatabaseSchema[dbs.length];
			int i = 0;

			logger.info("____________________________________________");
			logger.info("                                            ");
			logger.info("              OVSDB SCHEMA                  ");
			logger.info("____________________________________________");
			for (String database : dbs) {
				CompletableFuture<DatabaseSchema> s = client.getSchema(database);
				DatabaseSchema ds = s.get(3, TimeUnit.SECONDS);
				logger.info(ds.getName());
				logger.info("--------------------------------------------");
				for (String table : ds.getTables().keySet()) {
					logger.info(table);
					for (String colum : ds.getTables().get(table).getColumns().keySet()) {
						logger.info("   " + colum);
					}
				}
				schemas[i++] = ds;
			}
			return schemas;
		} catch (OvsdbClientException | InterruptedException | ExecutionException | TimeoutException e) {
			logger.error("Error while printing schema.", e);
			return null;
		}

	}

	@PreDestroy
	public void finish() {
		executorService.shutdown();
	}

	private OvsdbClient connect(String ip, Integer port) {
		try {
			CompletableFuture<OvsdbClient> clientFuture = connector.connect(ip, port); // (2)
			return clientFuture.get(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			logger.error("Error connecting to " + ip + ":" + port + ".");
			return null;
		}
	}

	private void close(OvsdbClient client) {
		if (client != null) {
			client.shutdown();
			client = null;
		}
	}
}
