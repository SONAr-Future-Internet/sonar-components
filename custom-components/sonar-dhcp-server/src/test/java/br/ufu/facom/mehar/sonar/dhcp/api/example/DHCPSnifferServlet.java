/*
 *	This file is part of dhcp4java, a DHCP API for the Java language.
 *	(c) 2006 Stephan Hadinger
 *
 *	This library is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU Lesser General Public
 *	License as published by the Free Software Foundation; either
 *	version 2.1 of the License, or (at your option) any later version.
 *
 *	This library is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *	Lesser General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public
 *	License along with this library; if not, write to the Free Software
 *	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package br.ufu.facom.mehar.sonar.dhcp.api.example;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.ufu.facom.mehar.sonar.dhcp.api.DHCPCoreServer;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPPacket;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPServerInitException;
import br.ufu.facom.mehar.sonar.dhcp.api.DHCPServlet;


/**
 * A simple DHCP sniffer based on DHCP servlets.
 *
 * @author Stephan Hadinger
 * @version 1.00
 */
public class DHCPSnifferServlet extends DHCPServlet {

    private static final Logger logger = Logger.getLogger("org.dhcp4java.examples.dhcpsnifferservlet");
    
    /**
     * Print received packet as INFO log, and do not respnd.
     * 
     * @see br.ufu.facom.mehar.sonar.dhcp.api.DHCPServlet#service(br.ufu.facom.mehar.sonar.dhcp.api.DHCPPacket)
     */
    @Override
    public DHCPPacket service(DHCPPacket request) {
        logger.info(request.toString());
        return null;
    }

    /**
     * Launcher for the server.
     * 
     * <p>No args.
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            DHCPCoreServer server = DHCPCoreServer.initServer(new DHCPSnifferServlet(), null);
            new Thread(server).start();
        } catch (DHCPServerInitException e) {
            logger.log(Level.SEVERE, "Server init", e);
        }
    }
}
