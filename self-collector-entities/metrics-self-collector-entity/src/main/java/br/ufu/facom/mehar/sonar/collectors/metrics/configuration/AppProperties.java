package br.ufu.facom.mehar.sonar.collectors.metrics.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppProperties {

	@Value("${amqp.broker.ip}")
	private String amqpBrokerIp;
	@Value("${amqp.exchange.name}")
	private String amqpExchangeName;
	@Value("${amqp.exchange.routing.key}")
	private String amqpExchangeRoutingKey;
	@Value("${amqp.exchange.type}")
	private String amqpExchangeType;
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
	@Value("${onos.api.base.path}")
	private String onosApiBasePath;
	@Value("${onos.api.statistics.ports.path}")
	private String onosApiStatisticsPortsPath;
	@Value("${onos.api.topology.clusters.path}")
	private String onosApiTopologyClustersPath;
	@Value("${onos.api.topology.clusters.devices.path}")
	private String onosApiTopologyClustersDevicesPath;
	@Value("${onos.ip}")
	private String onosIp;
	@Value("${onos.password}")
	private String onosPassword;
	@Value("${onos.port}")
	private Integer onosPort;
	@Value("${onos.user}")
	private String onosUser;

	public String getAmqpBrokerIp() {
		return amqpBrokerIp;
	}

	public void setAmqpBrokerIp(String amqpBrokerIp) {
		this.amqpBrokerIp = amqpBrokerIp;
	}

	public String getAmqpExchangeRoutingKey() {
		return amqpExchangeRoutingKey;
	}

	public void setAmqpExchangeRoutingKey(String amqpExchangeRoutingKey) {
		this.amqpExchangeRoutingKey = amqpExchangeRoutingKey;
	}

	public String getAmqpExchangeType() {
		return amqpExchangeType;
	}

	public void setAmqpExchangeType(String amqpExchangeType) {
		this.amqpExchangeType = amqpExchangeType;
	}

	public String getAmqpExchangeName() {
		return amqpExchangeName;
	}

	public void setAmqpExchangeName(String amqpExchangeName) {
		this.amqpExchangeName = amqpExchangeName;
	}

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

	public String getOnosApiBasePath() {
		return onosApiBasePath;
	}

	public void setOnosApiBasePath(String onosApiBasePath) {
		this.onosApiBasePath = onosApiBasePath;
	}

	public String getOnosApiStatisticsPortsPath() {
		return onosApiStatisticsPortsPath;
	}

	public void setOnosApiStatisticsPortsPath(String onosApiStatisticsPortsPath) {
		this.onosApiStatisticsPortsPath = onosApiStatisticsPortsPath;
	}

	public String getOnosApiTopologyClustersPath() {
		return onosApiTopologyClustersPath;
	}

	public void setOnosApiTopologyClustersPath(String onosApiTopologyClustersPath) {
		this.onosApiTopologyClustersPath = onosApiTopologyClustersPath;
	}

	public String getOnosApiTopologyClustersDevicesPath() {
		return onosApiTopologyClustersDevicesPath;
	}

	public void setOnosApiTopologyClustersDevicesPath(String onosApiTopologyClustersDevicesPath) {
		this.onosApiTopologyClustersDevicesPath = onosApiTopologyClustersDevicesPath;
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

}
