package br.ufu.facom.mehar.sonar.dhcp.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.ufu.facom.mehar.sonar.client.nem.configuration.SonarTopics;
import br.ufu.facom.mehar.sonar.client.nem.service.EventService;
import br.ufu.facom.mehar.sonar.core.model.event.IPAssignmentEvent;
import br.ufu.facom.mehar.sonar.core.util.IPUtils;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPPacket;

@Component
public class ConventionalDHCPEngine extends DHCPEngine{

	@Autowired
	private EventService eventService;

	@Override
	protected DHCPPacket doRequest(DHCPPacket request) {
		DHCPPacket response = super.doRequest(request);
		
		String mac = request.getChaddrAsHex();
		String ip = IPUtils.convertInetToIPString(response.getYiaddr());
		
		eventService.publish(SonarTopics.TOPIC_DHCP_IP_ASSIGNED, new IPAssignmentEvent(mac, ip));
		
		return response;
	}
}
