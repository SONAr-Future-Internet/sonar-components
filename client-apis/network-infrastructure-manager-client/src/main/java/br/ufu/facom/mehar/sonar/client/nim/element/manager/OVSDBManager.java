package br.ufu.facom.mehar.sonar.client.nim.element.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
import com.vmware.ovsdb.protocol.operation.Commit;
import com.vmware.ovsdb.protocol.operation.Insert;
import com.vmware.ovsdb.protocol.operation.Operation;
import com.vmware.ovsdb.protocol.operation.Select;
import com.vmware.ovsdb.protocol.operation.Update;
import com.vmware.ovsdb.protocol.operation.notation.Function;
import com.vmware.ovsdb.protocol.operation.notation.NamedUuid;
import com.vmware.ovsdb.protocol.operation.notation.Row;
import com.vmware.ovsdb.protocol.operation.notation.Uuid;
import com.vmware.ovsdb.protocol.operation.result.ErrorResult;
import com.vmware.ovsdb.protocol.operation.result.OperationResult;
import com.vmware.ovsdb.protocol.operation.result.SelectResult;
import com.vmware.ovsdb.protocol.schema.DatabaseSchema;
import com.vmware.ovsdb.service.OvsdbActiveConnectionConnector;
import com.vmware.ovsdb.service.OvsdbClient;
import com.vmware.ovsdb.service.impl.OvsdbActiveConnectionConnectorImpl;

import br.ufu.facom.mehar.sonar.client.nim.element.exception.BridgeNotFoundException;
import br.ufu.facom.mehar.sonar.client.nim.element.exception.DeviceConfigurationException;
import br.ufu.facom.mehar.sonar.client.nim.element.exception.MethodNotImplementedYetException;
import br.ufu.facom.mehar.sonar.core.model.configuration.Flow;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;

@Component("ovsdb")
public class OVSDBManager implements DeviceManager{
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
	private static final String BRIDGE_PROTOCOLS = "protocols";
	private static final String BRIDGE_CONTROLLER = "controller";
	
	private static final String FLOW_TABLE = "Flow_Table";

	private static final Integer TIMEOUT_IN_SECS = 3;

	private static final Integer DEFAULT_PORT = 6640;

	private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	private OvsdbActiveConnectionConnector connector = new OvsdbActiveConnectionConnectorImpl(executorService);
	
	@Override
	public Element discover(String ip) {
		throw new MethodNotImplementedYetException("Method 'discover' of OVSDBManager not implemented yet!");
	}
	
	@Override
	public void configureController(Element element, String[] controllerTargets) {
		String ip = element.getManagementIPAddressList().iterator().next();
		OvsdbClient client = connect(ip, DEFAULT_PORT);
		try {
			//Operations
			List<Operation> operations = new ArrayList<Operation>();
			
			/*
			 * Controllers
			 */
			
			Set<NamedUuid> controllerNamedUUIDs = new HashSet<NamedUuid>(controllerTargets.length);
			Set<Uuid> controllerUUIDs = new HashSet<Uuid>(controllerTargets.length);
			Set<String> controllersToConfigure = new HashSet<String>(controllerTargets.length);
			for(String sdnString : controllerTargets) {
				controllersToConfigure.add("tcp:"+sdnString);
			}
			SelectResult result = select(client, CONTROLLER);
			if (result != null && result.getRows() != null && !result.getRows().isEmpty()) {
				for (Row r : result.getRows()) {
					String controllerConfigured = r.getStringColumn(CONTROLLER_TARGET);
					if(controllersToConfigure.contains(controllerConfigured)) {
						Uuid uuid = r.getUuidColumn("_uuid");
						logger.info("Found controller "+controllerConfigured+" configured with uuid "+uuid);
						controllerUUIDs.add(uuid);
						controllersToConfigure.remove(controllerConfigured);
					}
				}
			}
			if(!controllersToConfigure.isEmpty()) {
				for(String controller : controllersToConfigure) {
					UUID uuid = UUID.randomUUID();
					String nameUUID = controller.replaceAll(":", "").replaceAll("\\.", "");
					Row r = new Row();
					r.uuidColumn("_uuid", new Uuid(uuid));
					r.stringColumn(CONTROLLER_TARGET, controller);
					controllerNamedUUIDs.add(new NamedUuid(nameUUID));
					operations.add(new Insert(CONTROLLER, r, nameUUID));
				}
			}
			
			
			/*
			 * Bridges 
			 */
			Set<String> bridgesToConfigure = new HashSet<String>(Arrays.asList("br0"));//configuring just br0
			Set<String> protocolSet = new HashSet<String>(Arrays.asList("OpenFlow10","OpenFlow11","OpenFlow12","OpenFlow13"));		
			result = select(client, BRIDGE);
			if (result != null && result.getRows() != null && !result.getRows().isEmpty()) {
				for (Row r : result.getRows()) {
					if(bridgesToConfigure.contains(r.getStringColumn(BRIDGE_NAME))) {
						bridgesToConfigure.remove(r.getStringColumn(BRIDGE_NAME));
						if(!r.getSetColumn(BRIDGE_PROTOCOLS).containsAll(protocolSet) || !r.getSetColumn(BRIDGE_CONTROLLER).containsAll(controllerUUIDs) || !controllerNamedUUIDs.isEmpty()) {
							Row rn = new Row();
							rn.setColumn(BRIDGE_PROTOCOLS, protocolSet);
							if(!controllerNamedUUIDs.isEmpty()) {
								rn.setColumn(BRIDGE_CONTROLLER, controllerNamedUUIDs);
							}else {
								rn.setColumn(BRIDGE_CONTROLLER, controllerUUIDs);
							}
							operations.add(new Update(BRIDGE, rn).where("_uuid", Function.EQUALS, r.getUuidColumn("_uuid")));
						}
					}
				}
				if(!bridgesToConfigure.isEmpty()) {
					//TODO Create bridge before configure the controller
					throw new BridgeNotFoundException("Bridges '"+bridgesToConfigure+"' not found in device "+ip+".");
				}
			}
			
			if(!operations.isEmpty()) {
				//Commit transaction and write to disk permanently
				operations.add(new Commit(Boolean.TRUE));
				
				CompletableFuture<OperationResult[]> resultFuture = client.transact("Open_vSwitch",operations );
				OperationResult[] resultList = resultFuture.get(10, TimeUnit.SECONDS);
				if (resultList != null && resultList.length > 0) {
					for(OperationResult operationResult : resultList) {
						
						
						if(operationResult instanceof ErrorResult) {
							ErrorResult errorResult = (ErrorResult) operationResult;
							logger.error(errorResult.getError()+". Details: "+errorResult.getDetails());
							throw new DeviceConfigurationException("Error '"+errorResult.getError()+"' while configuring controller in device "+ip+".");
						}
					}
				}
			} else {
				logger.info("It looks like the device is already configured with controller... Device:"+ip);
			}
		} catch (OvsdbClientException | InterruptedException | ExecutionException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			close(client);
		}
	}
	
	@Override
	public void configureFlows(Element element, Set<Flow> flows, Boolean permanent) {
		throw new MethodNotImplementedYetException("Method 'configureFlows' of OVSDBManager not implemented yet!");
	}
	
	/**
	 * UTIL
	 */

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