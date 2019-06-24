package br.ufu.facom.mehar.sonar.dhcp.engine;

import static br.ufu.facom.mehar.sonar.dhcp.api.DHCPConstants.BOOTREPLY;
import static br.ufu.facom.mehar.sonar.dhcp.api.DHCPConstants.BOOTREQUEST;
import static br.ufu.facom.mehar.sonar.dhcp.api.DHCPConstants.DHCPDECLINE;
import static br.ufu.facom.mehar.sonar.dhcp.api.DHCPConstants.DHCPDISCOVER;
import static br.ufu.facom.mehar.sonar.dhcp.api.DHCPConstants.DHCPINFORM;
import static br.ufu.facom.mehar.sonar.dhcp.api.DHCPConstants.DHCPRELEASE;
import static br.ufu.facom.mehar.sonar.dhcp.api.DHCPConstants.DHCPREQUEST;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.ufu.facom.mehar.sonar.client.nem.configuration.SonarTopics;
import br.ufu.facom.mehar.sonar.client.nem.service.EventService;
import br.ufu.facom.mehar.sonar.core.model.event.IPAssignmentEvent;
import br.ufu.facom.mehar.sonar.core.util.IPUtils;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPBadPacketException;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPPacket;

@Component
public class OpenflowDHCPEngine extends DHCPEngine{

	@Autowired
	private EventService eventService;
	
	public DatagramPacket serviceDatagram(String portInIp, String portInPort, DatagramPacket requestDatagram) {
		DatagramPacket responseDatagram;
    	
        if (requestDatagram == null) { return null; }

        try {
            // parse DHCP request
            DHCPPacket request = DHCPPacket.getPacket(requestDatagram);

            if (request == null) { return null; }	// nothing much we can do

            if (logger.isLoggable(Level.FINER)) {
                logger.finer(request.toString());
            }

            // do the real work
            DHCPPacket response = this.service(portInIp, portInPort, request); // call service function
            // done
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("service() done");
            }
            if (response == null) { return null; }

            // check address/port
            InetAddress address = response.getAddress();
            if (address == null) {
                logger.warning("Address needed in response");
                return null;
            }
            int port = response.getPort();

            // we have something to send back
            byte[] responseBuf = response.serialize();

            if (logger.isLoggable(Level.FINER)) { logger.finer("Buffer is " + responseBuf.length + " bytes long"); }

            responseDatagram = new DatagramPacket(responseBuf, responseBuf.length, address, port);
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("Sending back to" + address.getHostAddress() + '(' + port + ')');
            }
            this.postProcess(requestDatagram, responseDatagram);
            return responseDatagram;
        } catch (DHCPBadPacketException e) {
            logger.log(Level.INFO, "Invalid DHCP packet received", e);
        } catch (Exception e) {
            logger.log(Level.INFO, "Unexpected Exception", e);
        }

        // general fallback, we do nothing
        return null;
	}

	private DHCPPacket service(String portInIp, String portInPort, DHCPPacket request) {
		Byte dhcpMessageType;

        if (request == null) {
        	return null;
        }

        if (!request.isDhcp()) {
            logger.info("BOOTP packet rejected");
            return null;		// skipping old BOOTP
        }

        dhcpMessageType = request.getDHCPMessageType();

        if (dhcpMessageType == null) {
            logger.info("no DHCP message type");
            return null;
        }

        if (request.getOp() == BOOTREQUEST) {
            switch (dhcpMessageType) {
            	case DHCPDISCOVER: return this.doDiscover(request);
            	case DHCPREQUEST:  
            		DHCPPacket response = this.doRequest(request);
            		String mac = request.getChaddrAsHex();
            		String ip = IPUtils.convertInetToIPString(response.getYiaddr());
            		eventService.publish(SonarTopics.TOPIC_DHCP_IP_ASSIGNED, new IPAssignmentEvent(mac, ip, portInIp, portInPort));
            		return response;
            	case DHCPINFORM:   return this.doInform(request);
            	case DHCPDECLINE:  return this.doDecline(request);
            	case DHCPRELEASE:  return this.doRelease(request);

            	default:
            	    logger.info("Unsupported message type " + dhcpMessageType);
            	    return null;
            }
        } else if (request.getOp() == BOOTREPLY) {
            // receiving a BOOTREPLY from a client is not normal
            logger.info("BOOTREPLY received from client");
            return null;
        } else {
            logger.warning("Unknown Op: " + request.getOp());
            return null;	// ignore
        }
	}
}
