package br.ufu.facom.mehar.sonar.cim.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.cim.exception.PortPoolManagerException;

@Service
public class PortPoolService {
	private static final int firstport = 8100;
	private static final int lastport = 8199;

	private static final Map<String,List<Integer>> allocatedPorts = new HashMap<String,List<Integer>>();

	public Integer allocatePort(String server) {
		synchronized (allocatedPorts) {
			if(!allocatedPorts.containsKey(server)) {
				allocatedPorts.put(server, new ArrayList<Integer>());
			}
			
			for (int currentPort = firstport; currentPort < lastport; currentPort++) {
				if (!allocatedPorts.get(server).contains(currentPort)) {
					allocatedPorts.get(server).add(currentPort);
					return currentPort;
				}
			}
			throw new PortPoolManagerException("There is no available ports in the pool");
		}
	}

	public void allocatePort(String server, Integer port) {
		synchronized (allocatedPorts) {
			if(!allocatedPorts.containsKey(server)) {
				allocatedPorts.put(server, new ArrayList<Integer>());
			}
			
			if (allocatedPorts.get(server).contains(port)) {
				throw new PortPoolManagerException("There request port is already in use.");
			} else {
				allocatedPorts.get(server).add(port);
			}
		}
	}

	public void releasePort(String server, Integer port) {
		synchronized (allocatedPorts) {
			if(!allocatedPorts.containsKey(server)) {
				allocatedPorts.put(server, new ArrayList<Integer>());
			}
			
			if (allocatedPorts.get(server).contains(port)) {
				allocatedPorts.get(server).remove(port);
			}
		}
	}
}
