package br.ufu.facom.mehar.sonar.client.nim.component.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Configuration
public class NIMConfiguration {

	@Bean
	public RestTemplate restTemplate() {
		RestTemplate template = new RestTemplate();
		
		for(HttpMessageConverter<?> converter : template.getMessageConverters()) {
			if(converter.getClass().getName().equals(MappingJackson2HttpMessageConverter.class.getName())) {
				((MappingJackson2HttpMessageConverter)converter).setPrettyPrint(true);
				((MappingJackson2HttpMessageConverter)converter).getObjectMapper().setSerializationInclusion(Include.NON_NULL);
				((MappingJackson2HttpMessageConverter)converter).getObjectMapper().setSerializationInclusion(Include.NON_NULL);
			}
		}
		return template;
	}

	// Constants
	public static final String STRATEGY_ONOS = "onos";

	// Configuration
	private static String sdnNorthSeeds = "";
	private static String sdnStrategy = "";
	private static String sdnNorthAuthString;

	// Getters
	public String[] getSDNNorthSeeds() {
		return NIMConfiguration.sdnNorthSeeds.split(",");
	}

	public String getStrategy() {
		return NIMConfiguration.sdnStrategy;
	}

	public String getSdnNorthAuthString() {
		return NIMConfiguration.sdnNorthAuthString;
	}

	// Setters
	public static void setSDNNorthSeeds(String sdnNorthSeed) {
		NIMConfiguration.sdnNorthSeeds = sdnNorthSeed;
	}

	public static void setStrategy(String sdnStrategy) {
		NIMConfiguration.sdnStrategy = sdnStrategy;
	}

	public static void setSdnNorthAuthString(String sdnNorthAuthString) {
		NIMConfiguration.sdnNorthAuthString = sdnNorthAuthString;
	}

	// Change
	public static void addSeed(String seed) {
		if (sdnNorthSeeds != null && !sdnNorthSeeds.isEmpty()) {
			sdnNorthSeeds += "," + seed;
		} else {
			sdnNorthSeeds = seed;
		}
	}

	// Setters Instance Attributes
	@Value("${sonar.sdn.north.seeds:192.168.0.1:8181}")
	public void setSDNNorthSeedsAttr(String sdnNorthSeed) {
		NIMConfiguration.sdnNorthSeeds = sdnNorthSeed;
	}

	@Value("${sonar.sdn.strategy:onos}")
	public void setSDNStrategyAttr(String sdnStrategy) {
		NIMConfiguration.sdnStrategy = sdnStrategy;
	}

	@Value("${sonar.sdn.north.authString:onos:rocks}")
	public void setSDNNorthAuthStringAttr(String sdnNorthAuthString) {
		NIMConfiguration.sdnNorthAuthString = sdnNorthAuthString;
	}
}
