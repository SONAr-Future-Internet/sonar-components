package br.ufu.facom.mehar.sonar.organizing.configuration;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("br.ufu.facom.mehar.sonar")
public class App 
{
    public static void main( String[] args ){
    	new SpringApplicationBuilder(App.class).web(WebApplicationType.NONE).run(args);
    }
}
