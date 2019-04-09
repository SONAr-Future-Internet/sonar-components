package br.ufu.facom.mehar.sonar.collectors.topology.manager.lldp;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
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

import br.ufu.facom.mehar.sonar.core.model.topology.Element;
import br.ufu.facom.mehar.sonar.core.util.ObjectUtils;

public class LldpDiscoverManager {
	private Logger logger = Logger.getLogger(LldpDiscoverManager.class);

	private static final String LLDP_MIB = ".1.0.8802.1.1.2.1"; //http://www.mibdepot.com/cgi-bin/getmib3.cgi?win=mib_a&r=cisco&f=LLDP-MIB-V1SMI.my&v=v1&t=tree
	
	private static final String LLDP_MIB_REMOTE = LLDP_MIB+ ".4";
	private static final String LLDP_MIB_lldpRemSysName = LLDP_MIB_REMOTE+".1.1.9"; //hostname
	private static final String LLDP_MIB_lldpRemPortDesc = LLDP_MIB_REMOTE+".1.1.8"; //if-desc
	private static final String LLDP_MIB_lldpRemPortId = LLDP_MIB_REMOTE+".1.1.7"; //if-mac
	private static final String LLDP_MIB_lldpRemManAddrIfId = LLDP_MIB_REMOTE+".2.1.4"; //management-address and ifid
	
	private static final String LLDP_MIB_LOCAL = LLDP_MIB+ ".3";
	private static final String LLDP_MIB_lldpLocSysName = LLDP_MIB_LOCAL+".3.0"; //hostname
	private static final String LLDP_MIB_lldpLocPortDesc = LLDP_MIB_LOCAL+".7.1.4"; //if-desc
	private static final String LLDP_MIB_lldpLocPortId = LLDP_MIB_LOCAL+".7.1.3"; //if-mac
	private static final String LLDP_MIB_lldpLocManAddrIfId = LLDP_MIB_LOCAL+".8.1.5"; //management-address and ifid
	
	
	public Element discover(String ip) {
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString("secret"));
		target.setAddress(GenericAddress.parse("udp:"+ip+"/161")); // supply your own IP and port
		target.setRetries(2);
		target.setTimeout(1500);
		target.setVersion(SnmpConstants.version2c);

		
		try {
			Element element = new Element();
			if(element.getManagementIPAddressList() == null) {
				element.setManagementIPAddressList(new HashSet<String>());
			}
			
			// LLDP-MIB : 
	        Map<String, String> lldpInfo = doWalk(LLDP_MIB, target);
	        System.out.println("Element: "+ip);
	        for (Map.Entry<String, String> entry : lldpInfo.entrySet()) {
	        	if(entry.getKey().startsWith(LLDP_MIB_REMOTE)) {
	        		System.out.println(entry.getKey().substring(LLDP_MIB_REMOTE.length())+" -> "+entry.getValue());
	        		
	        		if(entry.getKey().startsWith(LLDP_MIB_lldpRemSysName)) {
	        			System.out.println(" RemSysName :: " + entry.getKey().substring(LLDP_MIB_lldpRemSysName.length())+" :: "+entry.getValue());
	        		}
	        		if(entry.getKey().startsWith(LLDP_MIB_lldpRemPortDesc)) {
	        			System.out.println(" RemPortDesc :: " + entry.getKey().substring(LLDP_MIB_lldpRemPortDesc.length())+" :: "+entry.getValue());
	        		}
	        		if(entry.getKey().startsWith(LLDP_MIB_lldpRemPortId)) {
	        			System.out.println(" RemPortId :: " + entry.getKey().substring(LLDP_MIB_lldpRemPortId.length())+" :: "+entry.getValue());
	        		}
	        		if(entry.getKey().startsWith(LLDP_MIB_lldpRemManAddrIfId)) {
	        			System.out.println(" RemManAddrIfId :: " + entry.getKey().substring(LLDP_MIB_lldpRemManAddrIfId.length())+" :: "+entry.getValue());
	        		}
	        	}
	        	
	        	if(entry.getKey().startsWith(LLDP_MIB_LOCAL)) {
	        		System.out.println(entry.getKey().substring(LLDP_MIB_LOCAL.length())+" -> "+entry.getValue());
	        		
	        		if(entry.getKey().startsWith(LLDP_MIB_lldpLocSysName)) {
	        			System.out.println(" LocSysName :: " + entry.getKey().substring(LLDP_MIB_lldpLocSysName.length())+" :: "+entry.getValue());
	        		}
	        		if(entry.getKey().startsWith(LLDP_MIB_lldpLocPortDesc)) {
	        			System.out.println(" LocPortDesc :: " + entry.getKey().substring(LLDP_MIB_lldpLocPortDesc.length())+" :: "+entry.getValue());
	        		}
	        		if(entry.getKey().startsWith(LLDP_MIB_lldpLocPortId)) {
	        			System.out.println(" LocPortId :: " + entry.getKey().substring(LLDP_MIB_lldpLocPortId.length())+" :: "+entry.getValue());
	        		}
	        		if(entry.getKey().startsWith(LLDP_MIB_lldpLocManAddrIfId)) {
	        			System.out.println(" LocManAddrIfId :: " + entry.getKey().substring(LLDP_MIB_lldpLocManAddrIfId.length())+" :: "+entry.getValue());
	        		}
	        	}
			}
	        
	        return element;
		} catch (IOException e) {
			logger.info("Unable to connect to "+ip);
			return null;
		}
	}

	public Map<String, String> doWalk(String tableOid, Target target) throws IOException {
		Map<String, String> result = new TreeMap<>();
		TransportMapping<? extends Address> transport = new DefaultUdpTransportMapping();
		Snmp snmp = new Snmp(transport);
		transport.listen();

		TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
		List<TreeEvent> events = treeUtils.getSubtree(target, new OID(tableOid));
		if (events == null || events.size() == 0) {
			logger.error("Error: Unable to read table...");
			return result;
		}

		for (TreeEvent event : events) {
			if (event == null) {
				continue;
			}
			if (event.isError()) {
				logger.error("Error: table OID [" + tableOid + "] " + event.getErrorMessage());
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
	
	public static void main(String[] args) {
		LldpDiscoverManager lldp = new LldpDiscoverManager();
		Element element = lldp.discover("192.168.1.1");
		System.out.println(ObjectUtils.toString(element));
	}
}