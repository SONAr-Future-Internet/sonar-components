package br.ufu.facom.mehar.sonar.collectors.metrics.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppProperties {

	@Value("${elastic.api.index.path}")
	private String elasticApiIndexPath;
	@Value("${elastic.ip}")
	private String elasticIp;
	@Value("${elastic.port}")
	private Integer elasticPort;
	@Value("${http.read.timeout}")
	private Integer httpConnectTimeout;
	@Value("${http.read.timeout}")
	private Integer httpReadTimeout;
	@Value("${onos.ip}")
	private String onosIp;
	@Value("${onos.password}")
	private String onosPassword;
	@Value("${onos.port}")
	private Integer onosPort;
	@Value("${onos.user}")
	private String onosUser;
	@Value("${ovsdb.completable.future.timeout}")
	private Integer ovsdbCompletableFutureTimeout;
	@Value("${ovsdb.database.name}")
	private String ovsdbDatabaseName;
	@Value("${ovsdb.table.name}")
	private String ovsdbTableName;
	@Value("${ovsdb.table.columns}")
	private String[] ovsdbTableColumns;
	@Value("${ovsdb.ips}")
	private String[] ovsdbIps;
	@Value("${ovsdb.port}")
	private Integer ovsdbPort;

	public String getElasticApiIndexPath() {
		return elasticApiIndexPath;
	}

	public void setElasticApiIndexPath(String elasticApiIndexPath) {
		this.elasticApiIndexPath = elasticApiIndexPath;
	}

	public String getElasticIp() {
		return elasticIp;
	}

	public void setElasticIp(String elasticIp) {
		this.elasticIp = elasticIp;
	}

	public Integer getElasticPort() {
		return elasticPort;
	}

	public void setElasticPort(Integer elasticPort) {
		this.elasticPort = elasticPort;
	}

	public Integer getHttpConnectTimeout() {
		return httpConnectTimeout;
	}

	public void setHttpConnectTimeout(Integer httpConnectTimeout) {
		this.httpConnectTimeout = httpConnectTimeout;
	}

	public Integer getHttpReadTimeout() {
		return httpReadTimeout;
	}

	public void setHttpReadTimeout(Integer httpReadTimeout) {
		this.httpReadTimeout = httpReadTimeout;
	}

	public String getOnosIp() {
		return onosIp;
	}

	public void setOnosIp(String onosIp) {
		this.onosIp = onosIp;
	}

	public String getOnosPassword() {
		return onosPassword;
	}

	public void setOnosPassword(String onosPassword) {
		this.onosPassword = onosPassword;
	}

	public Integer getOnosPort() {
		return onosPort;
	}

	public void setOnosPort(Integer onosPort) {
		this.onosPort = onosPort;
	}

	public String getOnosUser() {
		return onosUser;
	}

	public void setOnosUser(String onosUser) {
		this.onosUser = onosUser;
	}

	public Integer getOvsdbCompletableFutureTimeout() {
		return ovsdbCompletableFutureTimeout;
	}

	public void setOvsdbCompletableFutureTimeout(Integer ovsdbCompletableFutureTimeout) {
		this.ovsdbCompletableFutureTimeout = ovsdbCompletableFutureTimeout;
	}

	public String getOvsdbDatabaseName() {
		return ovsdbDatabaseName;
	}

	public void setOvsdbDatabaseName(String ovsdbDatabaseName) {
		this.ovsdbDatabaseName = ovsdbDatabaseName;
	}

	public String getOvsdbTableName() {
		return ovsdbTableName;
	}

	public void setOvsdbTableName(String ovsdbTableName) {
		this.ovsdbTableName = ovsdbTableName;
	}

	public String[] getOvsdbTableColumns() {
		return ovsdbTableColumns;
	}

	public void setOvsdbTableColumns(String[] ovsdbTableColumns) {
		this.ovsdbTableColumns = ovsdbTableColumns;
	}

	public String[] getOvsdbIps() {
		return ovsdbIps;
	}

	public void setOvsdbIps(String[] ovsdbIps) {
		this.ovsdbIps = ovsdbIps;
	}

	public Integer getOvsdbPort() {
		return ovsdbPort;
	}

	public void setOvsdbPort(Integer ovsdbPort) {
		this.ovsdbPort = ovsdbPort;
	}

}
