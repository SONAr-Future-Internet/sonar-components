package br.ufu.facom.mehar.sonar.organizing.configuration.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SCEConfiguration {
	//Constants
	public static final String STRATEGY_ONOS="onos";
	
	//Configuration
	private static String sdnSouthSeeds = "";
	private static String sdnStrategy = "";

	//Getters
	public String[] getSDNSouthSeeds() {
		return SCEConfiguration.sdnSouthSeeds.split(",");
	}
	
	public String getStrategy() {
		return SCEConfiguration.sdnStrategy;
	}
	
	//Setters
	public static void setSDNSouthSeeds(String sdnSouthSeed) {
		SCEConfiguration.sdnSouthSeeds = sdnSouthSeed;
	}
	
	public static void setStrategy(String sdnStrategy) {
		SCEConfiguration.sdnStrategy = sdnStrategy;
	}

	//Change
	public static void addSeed(String seed) {
		if (sdnSouthSeeds != null && !sdnSouthSeeds.isEmpty()) {
			sdnSouthSeeds += "," + seed;
		} else {
			sdnSouthSeeds = seed;
		}
	}
	
	//Setters Instance Attributes
	@Value("${sonar.sdn.south.seeds:192.168.0.1:6653}")
	public void setSDNSouthSeedsAttr(String sdnSouthSeed) {
		SCEConfiguration.sdnSouthSeeds = sdnSouthSeed;
	}
	
	@Value("${sonar.sdn.strategy:onos}")
	public void setSDNStrategyAttr(String sdnStrategy) {
		SCEConfiguration.sdnStrategy = sdnStrategy;
	}
}
