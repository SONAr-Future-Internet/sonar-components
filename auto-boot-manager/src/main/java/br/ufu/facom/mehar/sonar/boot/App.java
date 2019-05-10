package br.ufu.facom.mehar.sonar.boot;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan("br.ufu.facom.mehar.sonar")
@EnableAutoConfiguration
public class App {

	public static void main(String[] args) {
		new SpringApplicationBuilder(App.class)
        .web(WebApplicationType.NONE)
        .run(args);
	}
}
