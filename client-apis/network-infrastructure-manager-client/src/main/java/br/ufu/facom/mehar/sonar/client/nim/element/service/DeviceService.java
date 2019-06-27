package br.ufu.facom.mehar.sonar.client.nim.element.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.client.ndb.configuration.SonarProperties;
import br.ufu.facom.mehar.sonar.client.ndb.service.PropertyDataService;
import br.ufu.facom.mehar.sonar.client.nim.element.manager.impl.ONOSManager;
import br.ufu.facom.mehar.sonar.client.nim.element.manager.impl.OVSDBManager;
import br.ufu.facom.mehar.sonar.core.model.configuration.Configuration;
import br.ufu.facom.mehar.sonar.core.model.configuration.ConfigurationType;
import br.ufu.facom.mehar.sonar.core.model.configuration.Flow;
import br.ufu.facom.mehar.sonar.core.model.configuration.FlowInstruction;
import br.ufu.facom.mehar.sonar.core.model.core.Controller;
import br.ufu.facom.mehar.sonar.core.model.property.DataProperty;
import br.ufu.facom.mehar.sonar.core.model.topology.Element;
import br.ufu.facom.mehar.sonar.core.model.topology.Port;
import br.ufu.facom.mehar.sonar.core.model.topology.type.ElementType;
import br.ufu.facom.mehar.sonar.core.util.CountingLatch;

@Service
public class DeviceService {

	private Logger logger = LoggerFactory.getLogger(DeviceService.class);

	@Autowired
	private OVSDBManager ovsdbManager;
	
	@Autowired
	private ONOSManager onosManager;
	
	@Autowired 
	private PropertyDataService propertyService;
	
	public void configureController(Collection<Element> deviceSet, Controller controller, Boolean waitConnection) {
		this.configureController(deviceSet, Arrays.asList(controller), waitConnection);
	}

	public void configureController(Element element, List<Controller> controllerList, Boolean waitConnection) {
		ovsdbManager.configureController(element, extractSouthbound(controllerList));
		
		if(waitConnection) {
			waitConnection(controllerList, new HashSet<Element>(Arrays.asList(element)));
		}
	}
	
	public void configureController(final Collection<Element> deviceSet, final List<Controller> controllerList, Boolean waitConnection) {
		final ExecutorService taskExecutor = Executors.newFixedThreadPool(deviceSet.size() < 10? deviceSet.size(): 10);
		final CountingLatch latch = new CountingLatch(0);
		
		for(final Element element : deviceSet) {
			latch.countUp();
			taskExecutor.submit(new Runnable() {
				@Override
				public void run() {
					try {
						//Configure
						ovsdbManager.configureController(element, extractSouthbound(controllerList));
						
						//Set 'Data'
						if(element.getOfControllerList() == null) {
							element.setOfControllerList(new HashSet<UUID>());
						}
						for(Controller controller : controllerList) {
							element.getOfControllerList().add(controller.getIdController());
						}
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
			waitConnection(controllerList, deviceSet);
		}
	}
	
	public void waitConnection(Collection<Controller> controllerList, Collection<Element> deviceSet) {
		Map<String,Element> ipToElementMap = new HashMap<String,Element>();
		Map<String,Element> macToElementMap = new HashMap<String,Element>();
		for(final Element element : deviceSet) {
			for(String ip : element.getIpAddressList()) {
				ipToElementMap.put(ip, element);
				for(Port port : element.getPortList()) {
					macToElementMap.put(port.getMacAddress(), element);
				}
			}
		}
		
		int contElementWithoutSDNConnection = deviceSet.size();
		while(contElementWithoutSDNConnection != 0) {
			for(Controller controller : controllerList) {
				Collection<Element> sdnElementList = onosManager.discover(controller);
				for(Element sdnElement : sdnElementList) {
					String ipSdnElement = sdnElement.getIpAddressList().iterator().next();
					
					Element configuredElement = null;
					if(ipToElementMap.containsKey(ipSdnElement) && !ElementType.SERVER.equals(ipToElementMap.get(ipSdnElement).getTypeElement())) {
						configuredElement = ipToElementMap.get(ipSdnElement);
					}else {
						for(Port sdnPort : sdnElement.getPortList()) {
							if(macToElementMap.containsKey(sdnPort.getMacAddress())) {
								configuredElement = macToElementMap.get(sdnPort.getMacAddress());
								break;
							}
						}
						
						if(configuredElement == null && sdnElement.getOfChannel() != null) {
							DataProperty data = propertyService.getData(SonarProperties.APPLICATION_CONTROLLER_INTERCEPTOR, SonarProperties.INSTANCE_SHARED, SonarProperties.GROUP_CI_PATH_TO_IP, sdnElement.getOfChannel());
							if(data != null) {
								String ip = data.getValue();
								if(ipToElementMap.containsKey(ip)) {
									configuredElement = ipToElementMap.get(ip);
								}
							}
							
						}
					}
					if(configuredElement != null && configuredElement.getOfDeviceId() == null) {
						configuredElement.setOfDeviceId(sdnElement.getOfDeviceId());
						configuredElement.setOfChannel(sdnElement.getOfChannel());
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

	public void configure(Controller controller, Element element, List<Configuration> configurationList) {
		List<Flow> flowList = new ArrayList<Flow>();
		for(Configuration configuration : resolveReferences(element, configurationList)) {
			if(ConfigurationType.FLOW_CREATION.equals(configuration.getType())) {
				flowList.add(configuration.getFlow());
			}
		}
		
		if(element.getOfDeviceId() != null) {
			onosManager.configureFlows(controller, flowList, Boolean.TRUE);
		}
	}

	public void configure(Controller controller, Map<Element, List<Configuration>> configurationMap, Boolean waitConfiguration) {
		
		List<Flow> flowList = new ArrayList<Flow>();

		for(Element device : configurationMap.keySet()) {
			for(Configuration configuration : resolveReferences(device, configurationMap.get(device))) {
				if(ConfigurationType.FLOW_CREATION.equals(configuration.getType())) {
					flowList.add(configuration.getFlow());
				}
			}
		}
		
		onosManager.configureFlows(controller, flowList, waitConfiguration);
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
	
	private String[] extractSouthbound(List<Controller> controllerList) {
		String[] southboundInterfaces = new String[controllerList.size()];
		for(int i=0; i<controllerList.size(); i++) {
			if(controllerList.get(i).getInterceptor() != null) {
				southboundInterfaces[i] = controllerList.get(i).getInterceptor();
			}else {
				southboundInterfaces[i] = controllerList.get(i).getSouth();
			}
		}
		return southboundInterfaces;
	}
}
