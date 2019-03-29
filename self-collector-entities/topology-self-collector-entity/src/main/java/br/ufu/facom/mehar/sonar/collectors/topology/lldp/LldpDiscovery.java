package br.ufu.facom.mehar.sonar.collectors.topology.lldp;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

import br.ufu.facom.mehar.sonar.core.util.CountingLatch;

public class LldpDiscovery {
	private Logger logger = Logger.getLogger(LldpDiscovery.class);
	
	String discoveryRangeStart = "192.168.0.1";
	String discoveryRangeFinish = "192.168.0.255";

	String proactiveDiscoveryRangeStart = "192.168.0.1";
	String proactiveDiscoveryRangeFinish = "192.168.0.255";

	Stack<String> ipsToDiscovery = new Stack<String>();
	Stack<String> ipsToMonitore = new Stack<String>();
	
	private static final String LLDP_MIB = "1.0.8802.1.1.2.1"; //http://www.mibdepot.com/cgi-bin/getmib3.cgi?win=mib_a&r=cisco&f=LLDP-MIB-V1SMI.my&v=v1&t=tree
	
	private static final String LLDP_MIB_REMOTE = LLDP_MIB+ ".4";
	private static final String LLDP_MIB_lldpRemSysName = LLDP_MIB_REMOTE+".1.1.9"; //hostname
	private static final String LLDP_MIB_lldpRemPortDesc = LLDP_MIB_REMOTE+".1.1.8"; //if-desc
	private static final String LLDP_MIB_lldpRemPortId = LLDP_MIB_REMOTE+".1.1.7"; //if-mac
	private static final String LLDP_MIB_lldpRemManAddrIfId = LLDP_MIB_REMOTE+".2.1.4"; //management-address and ifid
	
	private static final String LLDP_MIB_LOCAL = LLDP_MIB+ ".3";
	private static final String LLDP_MIB_lldpLocSysName = LLDP_MIB_LOCAL+".3.0"; //hostname
	private static final String LLDP_MIB_lldpLocPortDesc = LLDP_MIB_LOCAL+".7.1.4"; //if-desc
	private static final String LLDP_MIB_lldpLocPortId = LLDP_MIB_REMOTE+".7.1.3"; //if-mac
	private static final String LLDP_MIB_lldpLocManAddrIfId = LLDP_MIB_REMOTE+".8.1.5"; //management-address and ifid
	
	
	class DiscoveryTask implements Runnable{
		private String ip;
		
		public DiscoveryTask(String ip) {
			super();
			this.ip = ip;
		}

		@Override
		public void run() {
			try {
				CommunityTarget target = new CommunityTarget();
				target.setCommunity(new OctetString("secret"));
				target.setAddress(GenericAddress.parse("udp:"+ip+"/161")); // supply your own IP and port
				target.setRetries(2);
				target.setTimeout(1500);
				target.setVersion(SnmpConstants.version2c);
		
				// LLDP-MIB : 
		        Map<String, String> lldpInfo = doWalk(LLDP_MIB, target);
		        for (Map.Entry<String, String> entry : lldpInfo.entrySet()) {
		        	System.out.println(ip + " => "+entry.getKey()+" : "+entry.getValue());
				}
			} catch (IOException e) {
				logger.info("Unable to connect to "+ip);
			}finally {
				
			}
		}
		
	}
	
	public void runDiscovery() {
		this.runDiscovery(false);
	}
	
	public void runDiscovery(Boolean proactive) {
		ExecutorService taskExecutor = Executors.newFixedThreadPool(4);
		
		
		if(!proactive) {
			final CountingLatch latch = new CountingLatch(0);
			
			while(latch.getCount() > 0 || !ipsToDiscovery.isEmpty()) {
				while(!ipsToDiscovery.isEmpty()) {
					latch.countUp();
					taskExecutor.execute(new DiscoveryTask(ipsToDiscovery.pop()) {
						@Override
						public void run() {
							super.run();
							latch.countDown();
						}
					});
				}
				
				try {
					if(latch.getCount() > 0 || !ipsToDiscovery.isEmpty()) {
						logger.info("Waiting... "+ latch.getCount()+" discovery tasks running and "+ipsToDiscovery.size()+" devices to discovery.");
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			System.out.println("Finished..");
		}
		
		taskExecutor.shutdown();
	}
	

	public static void main(String[] args) throws Exception {
		
		LldpDiscovery lldpDiscovery = new LldpDiscovery();
		lldpDiscovery.ipsToDiscovery.push("192.168.0.3");
		lldpDiscovery.ipsToDiscovery.push("192.168.0.4");
		
		lldpDiscovery.runDiscovery();
//		System.out.println("Running...");
//
//		CommunityTarget target = new CommunityTarget();
//		target.setCommunity(new OctetString("secret"));
//		target.setAddress(GenericAddress.parse("udp:localhost/161")); // supply your own IP and port
//		target.setRetries(2);
//		target.setTimeout(1500);
//		target.setVersion(SnmpConstants.version2c);
//
//		// LLDP-MIB
////        Map<String, String> lldpInfo = doWalk("1.0.8802.1.1.2.1.4.1.1", target);
//
//		// IP-MIB - ipNetToPhysicalTable (ARP)
//		Map<String, String> arpInfo = doWalk("1.0.8802.1.1.2.1.4.1.1", target);
//
//		System.out.println(arpInfo);
//
//		for (Map.Entry<String, String> entry : arpInfo.entrySet()) {
//			if (entry.getKey().startsWith(".1.3.6.1.2.1.2.2.1.2.")) {
//				System.out.println(
//						"ifDescr" + entry.getKey().replace(".1.3.6.1.2.1.2.2.1.2", "") + ": " + entry.getValue());
//			}
//			if (entry.getKey().startsWith(".1.3.6.1.2.1.2.2.1.3.")) {
//				System.out.println(
//						"ifType" + entry.getKey().replace(".1.3.6.1.2.1.2.2.1.3", "") + ": " + entry.getValue());
//			}
//		}
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