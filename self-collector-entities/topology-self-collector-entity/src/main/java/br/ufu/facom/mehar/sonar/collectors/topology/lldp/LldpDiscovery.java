package br.ufu.facom.mehar.sonar.collectors.topology.lldp;
import java.io.IOException;
import java.util.ArrayList;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
 
import org.snmp4j.CommunityTarget;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;
 
public class LldpDiscovery {
 
    public static void main(String[] args) throws Exception {
    	System.out.println("Running...");
    	
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("secret"));
        target.setAddress(GenericAddress.parse("udp:192.168.0.17/161")); // supply your own IP and port
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);
        
        //LLDP-MIB
        Map<String, String> lldpInfo = doWalk("1.0.8802.1.1.2.1.4.1.1", target);
        
        //IP-MIB - ipNetToPhysicalTable (ARP)
        Map<String, String> arpInfo = doWalk("1.0.8802.1.1.2.1.4.1.1", target);
 
        System.out.println(arpInfo);
        
        for (Map.Entry<String, String> entry : arpInfo.entrySet()) {
            if (entry.getKey().startsWith(".1.3.6.1.2.1.2.2.1.2.")) {
                System.out.println("ifDescr" + entry.getKey().replace(".1.3.6.1.2.1.2.2.1.2", "") + ": " + entry.getValue());
            }
            if (entry.getKey().startsWith(".1.3.6.1.2.1.2.2.1.3.")) {
                System.out.println("ifType" + entry.getKey().replace(".1.3.6.1.2.1.2.2.1.3", "") + ": " + entry.getValue());
            }
        }
    }
 
    public static Map<String, String> doWalk(String tableOid, Target target) throws IOException {
        Map<String, String> result = new TreeMap<>();
        TransportMapping<? extends Address> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();
 
        TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
        List<TreeEvent> events = treeUtils.getSubtree(target, new OID(tableOid));
        if (events == null || events.size() == 0) {
            System.out.println("Error: Unable to read table...");
            return result;
        }
 
        for (TreeEvent event : events) {
            if (event == null) {
                continue;
            }
            if (event.isError()) {
                System.out.println("Error: table OID [" + tableOid + "] " + event.getErrorMessage());
                continue;
            }
 
            VariableBinding[] varBindings = event.getVariableBindings();
            if (varBindings == null || varBindings.length == 0) {
                continue;
            }
            for (VariableBinding varBinding : varBindings) {
                if (varBinding == null) {
                    continue;
                }
                 
                result.put("." + varBinding.getOid().toString(), varBinding.getVariable().toString());
            }
 
        }
        snmp.close();
 
        return result;
    }
 
}