package br.ufu.facom.mehar.sonar.core.model.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Registry extends HashMap<String, Map<String, List<Container>>>{

	private static final long serialVersionUID = 3812259782579411725L;

	public void registerContainer(Container container){
		if(!this.containsKey(container.getServer())) {
			this.put(container.getServer(), new HashMap<String, List<Container>>());
		}
		
		 Map<String, List<Container>> namespaceMap = this.get(container.getServer());
		 if(!namespaceMap.containsKey(container.getNamespace())) {
			 namespaceMap.put(container.getNamespace(), new ArrayList<Container>());
		 }
		 
		 List<Container> containerList = namespaceMap.get(container.getNamespace());
		 if(containerList.contains(container)) {
			 containerList.remove(container);
		 }
		 containerList.add(container);
	}
	
	public void removeContainer(Container container) {
		if(!this.containsKey(container.getServer())) {
			return;
		}
		
		 Map<String, List<Container>> namespaceMap = this.get(container.getServer());
		 if(!namespaceMap.containsKey(container.getNamespace())) {
			 return;
		 }

		 List<Container> containerList = namespaceMap.get(container.getNamespace());
		 if(containerList.contains(container)) {
			 containerList.remove(container);
		 }
	}
	
	public void registerServer(String server) {
		if(!this.containsKey(server)) {
			this.put(server, new HashMap<String, List<Container>>());
		}
	}

	public void removeServer(String server) {
		if(this.containsKey(server)) {
			this.remove(server);
		}
	}
	
	public Set<String> getServers() {
		return this.keySet();
	}
	
	public List<Container> getContainers(){
		List<Container> containerList = new ArrayList<Container>();
		for(String server : this.keySet()) {
			containerList.addAll(this.getContainers(server));
		}
		return containerList;
	}
	
	public List<Container> getContainers(String server){
		List<Container> containerList = new ArrayList<Container>();
		
		Map<String, List<Container>> namespaceMap = this.get(server);
		for(String namespace : namespaceMap.keySet()) {
			List<Container> containerListAux = namespaceMap.get(namespace);
			for(Container container : containerListAux) {
				container.setServer(server);
				container.setNamespace(namespace);
				containerList.add(container);
			}
		}
		
		return containerList;
	}
}
