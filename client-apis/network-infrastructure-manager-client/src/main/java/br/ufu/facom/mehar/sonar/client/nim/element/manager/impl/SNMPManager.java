package br.ufu.facom.mehar.sonar.client.nim.element.manager.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.stereotype.Component;

import br.ufu.facom.mehar.sonar.client.nim.element.manager.ElementManager;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;
import br.ufu.facom.mehar.sonar.core.util.IPUtils;

@Component("snmp")
public class SNMPManager implements ElementManager{
	private Logger logger = LoggerFactory.getLogger(SNMPManager.class);

	private static final String LLDP_MIB = ".1.0.8802.1.1.2.1"; //http://www.mibdepot.com/cgi-bin/getmib3.cgi?win=mib_a&r=cisco&f=LLDP-MIB-V1SMI.my&v=v1&t=tree
	
	private static final String LLDP_MIB_REMOTE = LLDP_MIB+ ".4";
	private static final String LLDP_MIB_lldpRemHostname = LLDP_MIB_REMOTE+".1.1.9"; //hostname
	private static final String LLDP_MIB_lldpRemPortIfName = LLDP_MIB_REMOTE+".1.1.8"; //if-desc
	private static final String LLDP_MIB_lldpRemPortIfId = LLDP_MIB_REMOTE+".1.1.6"; //if-id
	private static final String LLDP_MIB_lldpRemPortMac = LLDP_MIB_REMOTE+".1.1.7"; //if-mac
	private static final String LLDP_MIB_lldpRemAddress = LLDP_MIB_REMOTE+".2.1.4"; //address
	private static final String LLDP_MIB_lldpRemAddressIPv4Sufix = "1.4"; //ipv4
	
	private static final String LLDP_MIB_LOCAL = LLDP_MIB+ ".3";
	private static final String LLDP_MIB_lldpLocHostame = LLDP_MIB_LOCAL+".3.0"; //hostname
	private static final String LLDP_MIB_lldpLocPortIfName = LLDP_MIB_LOCAL+".7.1.4"; //if-desc
	private static final String LLDP_MIB_lldpLocPortMac = LLDP_MIB_LOCAL+".7.1.3"; //if-mac
	private static final String LLDP_MIB_lldpLocAddress = LLDP_MIB_LOCAL+".8.1.5"; //address
	private static final String LLDP_MIB_lldpLocAddressIPv4 = LLDP_MIB_lldpLocAddress+".1.4"; //ipv4
	
	
	@Override
	public Element discover(String ip) {
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString("secret"));
		target.setAddress(GenericAddress.parse("udp:"+ip+"/161")); // supply your own IP and port
		target.setRetries(2);
		target.setTimeout(1500);
		target.setVersion(SnmpConstants.version2c);

		
		try {
			// LLDP-MIB : 
	        Map<String, String> lldpInfo = doWalk(LLDP_MIB, target);
	        if(lldpInfo == null || lldpInfo.isEmpty()) {
	        	logger.info("LLDP Discover failed: "+ip);
	        	return null;
	        }
	        
	        logger.info("LLDP Discover: "+ip);
	        
	        Element element = new Element();
			if(element.getManagementIPAddressList() == null) {
				element.setManagementIPAddressList(new HashSet<String>(Arrays.asList(ip)));
			}
	        
	        HashMap<String, Port> portMap = new HashMap<String, Port>();
	        
	        
	        for (Map.Entry<String, String> entry : lldpInfo.entrySet()) {
	        	if(entry.getKey().startsWith(LLDP_MIB_REMOTE)) {
	        		//logger.debug(entry.getKey().substring(LLDP_MIB_REMOTE.length())+" -> "+entry.getValue());
	        		
	        		if(entry.getKey().startsWith(LLDP_MIB_lldpRemHostname)) {
	        			String remoteIdentification = entry.getKey().substring(LLDP_MIB_lldpRemHostname.length()+1);
	        			String[] remoteIdentificationParts = remoteIdentification.split("\\.");
	        			if(remoteIdentificationParts.length == 3) {
	        				String ifId = remoteIdentificationParts[1];
	        	        			
    	        			if(!portMap.containsKey(ifId)) {
    	        				portMap.put(ifId, new Port());
    	        			}
	        	        			
    	        			//setting remote hostname
    	        			portMap.get(ifId).setRemoteHostname(entry.getValue());
	        			}
	        			logger.debug(" RemHostname :: " + entry.getKey().substring(LLDP_MIB_lldpRemHostname.length()+1)+" :: "+entry.getValue());
	        		}
	        		if(entry.getKey().startsWith(LLDP_MIB_lldpRemPortIfId)) {
	        			String remoteIdentification = entry.getKey().substring(LLDP_MIB_lldpRemPortIfId.length()+1);
	        			String[] remoteIdentificationParts = remoteIdentification.split("\\.");
	        			if(remoteIdentificationParts.length == 3) {
	        				String ifId = remoteIdentificationParts[1];
	        	        			
    	        			if(!portMap.containsKey(ifId)) {
    	        				portMap.put(ifId, new Port());
    	        			}
	        	        			
    	        			//setting remote port id
    	        			portMap.get(ifId).setRemotePortId(entry.getValue());
	        			}
	        			logger.debug(" RemoteIfId :: " + entry.getKey().substring(LLDP_MIB_lldpRemHostname.length()+1)+" :: "+entry.getValue());
	        		}
	        		if(entry.getKey().startsWith(LLDP_MIB_lldpRemPortIfName)) {
	        			String remoteIdentification = entry.getKey().substring(LLDP_MIB_lldpRemHostname.length()+1);
	        			String[] remoteIdentificationParts = remoteIdentification.split("\\.");
	        			if(remoteIdentificationParts.length == 3) {
	        				String ifId = remoteIdentificationParts[1];
	        	        			
    	        			if(!portMap.containsKey(ifId)) {
    	        				portMap.put(ifId, new Port());
    	        			}
	        	        			
    	        			//setting remote port name
    	        			portMap.get(ifId).setRemotePortName(entry.getValue());
	        			}
	        			logger.debug(" RemoteIfName :: " + entry.getKey().substring(LLDP_MIB_lldpRemPortIfName.length()+1)+" :: "+entry.getValue());
	        		}
	        		if(entry.getKey().startsWith(LLDP_MIB_lldpRemPortMac)) {
	        			String remoteIdentification = entry.getKey().substring(LLDP_MIB_lldpRemPortMac.length()+1);
	        			String[] remoteIdentificationParts = remoteIdentification.split("\\.");
	        			if(remoteIdentificationParts.length == 3) {
	        				String ifId = remoteIdentificationParts[1];
	        	        			
    	        			if(!portMap.containsKey(ifId)) {
    	        				portMap.put(ifId, new Port());
    	        			}
	        	        			
    	        			//setting remote port mac
    	        			portMap.get(ifId).setRemoteMacAddress( IPUtils.normalizeMAC(entry.getValue()) );
	        			}
	        			logger.debug(" RemPortMac :: " + entry.getKey().substring(LLDP_MIB_lldpRemPortMac.length()+1)+" :: "+entry.getValue());
	        		}
	        		if(entry.getKey().startsWith(LLDP_MIB_lldpRemAddress)) {
	        			String remoteIdentification = entry.getKey().substring(LLDP_MIB_lldpRemHostname.length()+1);
	        			String[] remoteIdentificationParts = remoteIdentification.split("\\.");
	        			if(remoteIdentificationParts.length >= 9) {
	        				if( LLDP_MIB_lldpRemAddressIPv4Sufix.equals( remoteIdentificationParts[3]+"."+remoteIdentificationParts[4] )) {
	        					String ifId = remoteIdentificationParts[1];
        	        			
	    	        			if(!portMap.containsKey(ifId)) {
	    	        				portMap.put(ifId, new Port());
	    	        			}
		        	        			
	    	        			//setting remote port ip
	    	        			portMap.get(ifId).setRemoteIpAddress(remoteIdentificationParts[remoteIdentificationParts.length-4] + "." + remoteIdentificationParts[remoteIdentificationParts.length-3] + "." + remoteIdentificationParts[remoteIdentificationParts.length-2] + "." + remoteIdentificationParts[remoteIdentificationParts.length-1]);
	        				}
	        			}
	        			logger.debug(" RemAddress :: " + entry.getKey().substring(LLDP_MIB_lldpRemAddress.length()+1)+" :: "+entry.getValue());
	        		}
	        	}
	        	
	        	if(entry.getKey().startsWith(LLDP_MIB_LOCAL)) {
//	        		logger.debug(entry.getKey().substring(LLDP_MIB_LOCAL.length())+" -> "+entry.getValue());
	        		
	        		if(entry.getKey().startsWith(LLDP_MIB_lldpLocHostame)) {
	        			element.setName( entry.getValue() );
	        			logger.debug(" LocHostame :: "+entry.getValue());
	        		}
	        		if(entry.getKey().startsWith(LLDP_MIB_lldpLocPortIfName)) {
	        			String ifId = entry.getKey().substring(LLDP_MIB_lldpLocPortIfName.length()+1);
	        			if(!portMap.containsKey(ifId)) {
	        				portMap.put(ifId, new Port());
	        			}
	        			
	        			//setting local port id
	        			portMap.get(ifId).setPortId(ifId);
	        			
	        			//setting local port name
	        			portMap.get(ifId).setPortName(entry.getValue());
	        			logger.debug(" LocPortIfName :: " + entry.getKey().substring(LLDP_MIB_lldpLocPortIfName.length()+1)+" :: "+entry.getValue());
	        		}
	        		if(entry.getKey().startsWith(LLDP_MIB_lldpLocPortMac)) {
	        			String ifId = entry.getKey().substring(LLDP_MIB_lldpLocPortMac.length()+1);
	        			if(!portMap.containsKey(ifId)) {
	        				portMap.put(ifId, new Port());
	        			}
	        			
	        			//setting local port id
	        			portMap.get(ifId).setPortId(ifId);
	        			
	        			//setting local port mac
	        			portMap.get(ifId).setMacAddress( IPUtils.normalizeMAC(entry.getValue() ));
	        			logger.debug(" LocPortMac :: " + entry.getKey().substring(LLDP_MIB_lldpLocPortMac.length()+1)+" :: "+entry.getValue());
	        		}
	        		if(entry.getKey().startsWith(LLDP_MIB_lldpLocAddressIPv4)) {
	        			String ifId = entry.getValue();
	        			if(!portMap.containsKey(ifId)) {
	        				portMap.put(ifId, new Port());
	        			}
	        			
	        			//setting local port id
	        			portMap.get(ifId).setPortId(ifId);
	        			
	        			//setting local port ip
	        			String[] valueRaw = entry.getKey().substring(LLDP_MIB_lldpLocAddressIPv4.length()+1).split("\\.");
	        			if(valueRaw.length >= 4) {
	        				portMap.get(ifId).setIpAddress(valueRaw[valueRaw.length-4] + "." + valueRaw[valueRaw.length-3] + "." + valueRaw[valueRaw.length-2] + "." + valueRaw[valueRaw.length-1]);
	        			}
	        			logger.debug(" LocAddressIPv4 :: " + entry.getKey().substring(LLDP_MIB_lldpLocAddressIPv4.length()+1)+" :: "+entry.getValue());
	        		}
	        	}
			}
	        
	        //Remove interfaces without mac-address
	        for(String key : new HashSet<String>(portMap.keySet())) {
	        	Port port = portMap.get(key);
	        	if(port.getMacAddress() == null || port.getMacAddress().isEmpty()) {
	        		portMap.remove(key);
	        	}
	        }
	        
	        
	        if(logger.isDebugEnabled()) {
	        	logger.debug("Element "+element.getName()+" ("+element.getManagementIPAddressList()+") discovered...");
		        for(String key : portMap.keySet()) {
		        	logger.debug(
		        			String.format("[%-5s]\t%-8s\t%-12s\t%-20s\t%-15s\t<--->\t%-20s\t%-8s\t%-12s\t%-20s\t%-15s",	        			
		        			key,
		        			portMap.get(key).getPortId(),
		        			portMap.get(key).getPortName(),
		        			portMap.get(key).getMacAddress(),
		        			portMap.get(key).getIpAddress(),
		        			portMap.get(key).getRemoteHostname(),
		        			portMap.get(key).getRemotePortId(),
		        			portMap.get(key).getRemotePortName(),
		        			portMap.get(key).getRemoteMacAddress(),
		        			portMap.get(key).getRemoteIpAddress()) );
		        }
	        }
	        
	        element.setPortList(new HashSet<Port>(portMap.values()));
	        
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
}