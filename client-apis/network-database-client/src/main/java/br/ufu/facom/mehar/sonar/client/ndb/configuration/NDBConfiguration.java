package br.ufu.facom.mehar.sonar.client.ndb.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NDBConfiguration {
	//Constants
	public static final String STRATEGY_CASSANDRA="cassandra";
	
	//Configuration
	private static String seeds = "";
	private static String strategy = "";

	//Getters
	public String[] getSeeds() {
		return NDBConfiguration.seeds.split(",");
	}
	
	public String getStrategy() {
		return NDBConfiguration.strategy;
	}
	
	//Setters
	public static void setSeeds(String nddbSeeds) {
		NDBConfiguration.seeds = nddbSeeds;
	}
	
	public static void setStrategy(String strategy) {
		NDBConfiguration.strategy = strategy;
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
	@Value("${sonar.ndb.seeds:127.0.0.1:9042}")
	public void setSeedsAttr(String nddbSeeds) {
		NDBConfiguration.seeds = nddbSeeds;
	}
	
	@Value("${sonar.ndb.strategy:cassandra}")
	public void setStrategyAttr(String strategy) {
		NDBConfiguration.strategy = strategy;
	}
}
