package br.ufu.facom.mehar.sonar.core.model.core;

import java.util.UUID;

public class Controller {
	private UUID idController;
	private String north;
	private String south;
	private String interceptor;
	private String strategy;
	private String authUsername;
	private String authPassword;
	
	public UUID getIdController() {
		return idController;
	}
	public void setIdController(UUID idController) {
		this.idController = idController;
	}
	public String getNorth() {
		return north;
	}
	public void setNorth(String north) {
		this.north = north;
	}
	public String getSouth() {
		return south;
	}
	public void setSouth(String south) {
		this.south = south;
	}
	public String getInterceptor() {
		return interceptor;
	}
	public void setInterceptor(String interceptor) {
		this.interceptor = interceptor;
	}
	public String getStrategy() {
		return strategy;
	}
	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}
	public String getAuthUsername() {
		return authUsername;
	}
	public void setAuthUsername(String authUsername) {
		this.authUsername = authUsername;
	}
	public String getAuthPassword() {
		return authPassword;
	}
	public void setAuthPassword(String authPassword) {
		this.authPassword = authPassword;
	}
}
