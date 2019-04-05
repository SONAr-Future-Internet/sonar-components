package br.ufu.facom.mehar.sonar.client.dndb.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DNDBConfiguration {
	//Constants
	public static final String STRATEGY_CASSANDRA="cassandra";
	
	//Configuration
	private static String seeds = "";
	private static String strategy = "";

	//Getters
	public String[] getSeeds() {
		return DNDBConfiguration.seeds.split(",");
	}
	
	public String getStrategy() {
		return DNDBConfiguration.strategy;
	}
	
	//Setters
	public static void setSeeds(String nddbSeeds) {
		DNDBConfiguration.seeds = nddbSeeds;
	}
	
	public static void setStrategy(String strategy) {
		DNDBConfiguration.strategy = strategy;
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
	@Value("${sonar.dndb.seeds:127.0.0.1:9042}")
	public void setSeedsAttr(String nddbSeeds) {
		DNDBConfiguration.seeds = nddbSeeds;
	}
	
	@Value("${sonar.dndb.strategy:cassandra}")
	public void setStrategyAttr(String strategy) {
		DNDBConfiguration.strategy = strategy;
	}
}
