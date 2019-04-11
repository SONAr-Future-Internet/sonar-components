package br.ufu.facom.mehar.sonar.organizing.configuration.manager;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class KeepAliveManager {

	@Scheduled(fixedDelay = 600000000)
	public void keepRunning() throws InterruptedException {
		//Do nothing! Just keep the aplication running!
	}
}
