package br.ufu.facom.mehar.sonar.client.nim.element.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.client.nim.element.manager.impl.ONOSManager;
import br.ufu.facom.mehar.sonar.client.nim.element.manager.impl.OVSDBManager;
import br.ufu.facom.mehar.sonar.core.model.configuration.Configuration;
import br.ufu.facom.mehar.sonar.core.model.configuration.ConfigurationType;
import br.ufu.facom.mehar.sonar.core.model.configuration.Flow;
import br.ufu.facom.mehar.sonar.core.model.configuration.FlowInstruction;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;
import br.ufu.facom.mehar.sonar.core.util.CountingLatch;

@Service
public class DeviceService {

	private Logger logger = LoggerFactory.getLogger(DeviceService.class);

	@Autowired
	private OVSDBManager ovsdbManager;
	
	@Autowired
	private ONOSManager onosManager;
	
	public void configureController(Element element, String[] controllers) {
		//TODO Verify Vendor/Model
		ovsdbManager.configureController(element, controllers);
	}

	public void configure(Element element, List<Configuration> configurationList) {
		//TODO Verify Vendor/Model and Type of Configuration
		List<Flow> flowList = new ArrayList<Flow>();
		for(Configuration configuration : resolveReferences(element, configurationList)) {
			if(ConfigurationType.FLOW_CREATION.equals(configuration.getType())) {
				flowList.add(configuration.getFlow());
			}
		}
		
		if(element.getOfDeviceId() != null) {
			onosManager.configureFlows(flowList, Boolean.TRUE);
		}
	}

	public void configureController(final Set<Element> deviceSet, final String[] controllers, final Boolean waitConnection) {
		final ExecutorService taskExecutor = Executors.newFixedThreadPool(deviceSet.size() < 10? deviceSet.size(): 10);
		final CountingLatch latch = new CountingLatch(0);
		final Map<String,Element> ipMap = new HashMap<String,Element>();
		
		for(final Element element : deviceSet) {
			for(String ip : element.getManagementIPAddressList()) {
				ipMap.put(ip, element);
			}
			
			latch.countUp();
			taskExecutor.submit(new Runnable() {
				@Override
				public void run() {
					try {
//						System.out.println("Configuring controller on "+element.getName());
						ovsdbManager.configureController(element, controllers);
					}finally {
						latch.countDown();
					}
				}
			});
		}
			
		while (latch.getCount() != 0) {
			try {
				logger.info("Waiting... " + latch.getCount() + " controller configurations!");
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		taskExecutor.shutdown();
			
		if(waitConnection) {
			int contElementWithoutSDNConnection = deviceSet.size();
			while(contElementWithoutSDNConnection != 0) {
			
				Collection<Element> sdnElementList = onosManager.discover();
				for(Element sdnElement : sdnElementList) {
					String ipSdnElement = sdnElement.getManagementIPAddressList().iterator().next();
					if(ipMap.containsKey(ipSdnElement)) {
						Element configuredElement = ipMap.get(ipSdnElement);
						if(configuredElement.getOfDeviceId() == null) {
							configuredElement.setOfDeviceId(sdnElement.getOfDeviceId());
							Map<String,Port> macOrNameToPort = new HashMap<String, Port>();
							for(Port port : configuredElement.getPortList()) {
								macOrNameToPort.put(port.getMacAddress(), port);
								macOrNameToPort.put(port.getPortName(), port);
							}
							for(Port sdnPort : sdnElement.getPortList()) {
								if(macOrNameToPort.containsKey(sdnPort.getMacAddress())) {
									Port port = macOrNameToPort.get(sdnPort.getMacAddress());
									port.setOfPort(sdnPort.getOfPort());
								}else {
									if(macOrNameToPort.containsKey(sdnPort.getPortName())) {
										Port port = macOrNameToPort.get(sdnPort.getPortName());
										port.setPortName(sdnPort.getPortName());
										port.setOfPort(sdnPort.getOfPort());
									}
								}
							}
						}
					}
				}
				
				for(Element element : deviceSet) {
					if(element.getOfDeviceId() != null) {
						contElementWithoutSDNConnection--;
					}else {
						logger.info("Element "+element.getName()+" not connected yet!");
					}
				}
				
				if(contElementWithoutSDNConnection != 0) {
					contElementWithoutSDNConnection = deviceSet.size();
					try {
						logger.info("Waiting... " + contElementWithoutSDNConnection + " controller connection!");
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void configure(Map<Element, List<Configuration>> configurationMap, Boolean waitConfiguration) {
		
		List<Flow> bootFlowList = new ArrayList<Flow>();

		for(Element device : configurationMap.keySet()) {
			for(Configuration configuration : resolveReferences(device, configurationMap.get(device))) {
				if(ConfigurationType.FLOW_CREATION.equals(configuration.getType())) {
//					System.out.println("Configuring "+configuration.getIdElement()+" => "+configuration.getIdentification());
					bootFlowList.add(configuration.getFlow());
				}
			}
		}
		
//		System.out.println("Configuring "+bootFlowList.size()+" flows.");
		onosManager.configureFlows(bootFlowList, Boolean.FALSE);
	}

	private List<Configuration> resolveReferences(Element device, List<Configuration> configurationList) {
		Map<UUID,String> mapOfPort = new HashMap<UUID, String>();
		
		for(Port port : device.getPortList()) {
			if(port.getOfPort() != null) {
				mapOfPort.put(port.getIdPort(), port.getOfPort());
			}
		}
		
		for(Configuration configuration : configurationList) {
			if(configuration.getFlow() != null) {
				String ofDeviceId = configuration.getFlow().getDeviceId();
				if(ofDeviceId == null) {
					if(device.getOfDeviceId() != null) {
						configuration.getFlow().setDeviceId(device.getOfDeviceId());
					}else {
						logger.error("Error while converting idElement in ofDeviceId. idPort:"+configuration.getFlow().getDeviceRef());
					}
				}
				for(FlowInstruction instruction : configuration.getFlow().getInstructions()) {
					if(instruction.getValue() == null && instruction.getRefValue() != null) {
						String ofPort = mapOfPort.get(instruction.getRefValue());
						if(ofPort != null) {
							instruction.setValue(ofPort);
						}else {
							logger.error("Error while converting idPort in ofPort. idPort:"+instruction.getRefValue());
						}
					}
				}
			}
		}
		return configurationList;
	}
}
