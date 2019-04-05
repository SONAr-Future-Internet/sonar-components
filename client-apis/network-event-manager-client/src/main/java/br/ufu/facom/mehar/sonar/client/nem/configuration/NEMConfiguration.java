package br.ufu.facom.mehar.sonar.client.nem.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NEMConfiguration {
	//Constants
	public static final String STRATEGY_RABBIT="rabbitmq";
	
	//Configuration
	private static String seeds = "";
	private static String strategy = "";

	//Getters
	public String[] getSeeds() {
		return NEMConfiguration.seeds.split(",");
	}
	
	public String getStrategy() {
		return NEMConfiguration.strategy;
	}
	
	//Setters
	public static void setSeeds(String nddbSeeds) {
		NEMConfiguration.seeds = nddbSeeds;
	}
	
	public static void setStrategy(String strategy) {
		NEMConfiguration.strategy = strategy;
	}

	//Change
	public static void addSeed(String seed) {
		if (seeds != null && !seeds.isEmpty()) {
			seeds += "," + seed;
		} else {
			seeds = seed;
		}
	}
	
	//Setters Instance Attributes
	@Value("${sonar.nem.seeds:127.0.0.1:5672}")
	public void setSeedsAttr(String nddbSeeds) {
		NEMConfiguration.seeds = nddbSeeds;
	}
	
	@Value("${sonar.nem.strategy:rabbitmq}")
	public void setStrategyAttr(String strategy) {
		NEMConfiguration.strategy = strategy;
	}
}
