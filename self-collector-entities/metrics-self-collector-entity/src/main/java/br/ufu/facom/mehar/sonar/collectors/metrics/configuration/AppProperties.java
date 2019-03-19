package br.ufu.facom.mehar.sonar.collectors.metrics.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppProperties {

	@Value(value = "${devices.list}")
	private String[] devicesList;
	@Value(value = "${http.read.timeout}")
	private Integer httpReadTimeout;
	@Value(value = "${http.read.timeout}")
	private Integer httpConnectTimeout;
	@Value(value = "${statistics.base.endpoint}")
	private String statisticsBaseEndpoint;
	@Value(value = "${onos.ip}")
	private String onosIp;
	@Value(value = "${onos.port}")
	private String onosPort;
	@Value(value = "${onos.user}")
	private String onosUser;
	@Value(value = "${onos.password}")
	private String onosPassword;
	@Value(value = "${elastic.ip}")
	private String elasticIp;
	@Value(value = "${elastic.port}")
	private String elasticPort;
	@Value(value = "${elastic.endpoint}")
	private String elasticEndpoint;

	public String[] getDevicesList() {
		return devicesList;
	}

	public void setDevicesList(String[] devicesList) {
		this.devicesList = devicesList;
	}

	public Integer getHttpReadTimeout() {
		return httpReadTimeout;
	}

	public void setHttpReadTimeout(Integer httpReadTimeout) {
		this.httpReadTimeout = httpReadTimeout;
	}

	public Integer getHttpConnectTimeout() {
		return httpConnectTimeout;
	}

	public void setHttpConnectTimeout(Integer httpConnectTimeout) {
		this.httpConnectTimeout = httpConnectTimeout;
	}

	public String getStatisticsBaseEndpoint() {
		return statisticsBaseEndpoint;
	}

	public void setStatisticsBaseEndpoint(String statisticsBaseEndpoint) {
		this.statisticsBaseEndpoint = statisticsBaseEndpoint;
	}

	public String getOnosIp() {
		return onosIp;
	}

	public void setOnosIp(String onosIp) {
		this.onosIp = onosIp;
	}

	public String getOnosPort() {
		return onosPort;
	}

	public void setOnosPort(String onosPort) {
		this.onosPort = onosPort;
	}

	public String getOnosUser() {
		return onosUser;
	}

	public void setOnosUser(String onosUser) {
		this.onosUser = onosUser;
	}

	public String getOnosPassword() {
		return onosPassword;
	}

	public void setOnosPassword(String onosPassword) {
		this.onosPassword = onosPassword;
	}

	public String getElasticIp() {
		return elasticIp;
	}

	public void setElasticIp(String elasticIp) {
		this.elasticIp = elasticIp;
	}

	public String getElasticPort() {
		return elasticPort;
	}

	public void setElasticPort(String elasticPort) {
		this.elasticPort = elasticPort;
	}

	public String getElasticEndpoint() {
		return elasticEndpoint;
	}

	public void setElasticEndpoint(String elasticEndpoint) {
		this.elasticEndpoint = elasticEndpoint;
	}

}
