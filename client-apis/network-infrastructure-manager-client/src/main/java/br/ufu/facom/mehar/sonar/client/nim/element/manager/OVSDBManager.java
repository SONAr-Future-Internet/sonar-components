package br.ufu.facom.mehar.sonar.client.nim.element.manager;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.stereotype.Component;

import com.vmware.ovsdb.exception.OvsdbClientException;
import com.vmware.ovsdb.service.OvsdbActiveConnectionConnector;
import com.vmware.ovsdb.service.OvsdbClient;
import com.vmware.ovsdb.service.impl.OvsdbActiveConnectionConnectorImpl;

@Component("ovsdb")
public class OVSDBManager {

	public void test() throws InterruptedException, ExecutionException, TimeoutException, OvsdbClientException {
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();    
		OvsdbActiveConnectionConnector connector = new OvsdbActiveConnectionConnectorImpl(executorService);  // (1)

		CompletableFuture<OvsdbClient> ovsdbClientFuture = connector.connect("192.168.0.2", 6640);       // (2)

		OvsdbClient ovsdbClient = ovsdbClientFuture.get(3, TimeUnit.SECONDS);   // (3)
		CompletableFuture<String[]> f = ovsdbClient.listDatabases();
		String[] dbs = f.get(3, TimeUnit.SECONDS);
		System.out.println(Arrays.toString(dbs));
	}
}
