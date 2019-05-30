package br.ufu.facom.mehar.sonar.interceptor;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan("br.ufu.facom.mehar.sonar")
public class App {

	public static void main(String[] args) {
		new SpringApplicationBuilder(App.class).web(WebApplicationType.NONE).run(args);
	}
}
