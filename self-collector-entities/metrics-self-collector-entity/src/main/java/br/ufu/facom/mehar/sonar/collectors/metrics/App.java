package br.ufu.facom.mehar.sonar.collectors.metrics;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAutoConfiguration
@EnableScheduling
@ComponentScan("br.ufu.facom.mehar.sonar")
public class App 
{
    public static void main( String[] args ){
    	new SpringApplicationBuilder(App.class).web(WebApplicationType.NONE).run(args);
    }
}
