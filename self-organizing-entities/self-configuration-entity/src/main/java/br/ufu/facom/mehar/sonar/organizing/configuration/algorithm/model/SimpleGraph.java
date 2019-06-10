package br.ufu.facom.mehar.sonar.organizing.configuration.algorithm.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import br.ufu.facom.mehar.sonar.core.model.topology.Element;

public class SimpleGraph<T> {
	private Map<Node<T>, Set<Node<T>>> mapAdjacences = new HashMap<Node<T>, Set<Node<T>>>();

	public void addLink(T valueA, T valueB) {
		Node<T> nodeA = getNodeByValue(valueA);
		if (nodeA == null) {
			nodeA = new Node<T>(valueA);
			mapAdjacences.put(nodeA, new HashSet<Node<T>>());
		}

		Node<T> nodeB = getNodeByValue(valueB);
		if (nodeB == null) {
			nodeB = new Node<T>(valueB);
			mapAdjacences.put(nodeB, new HashSet<Node<T>>());
		}

		mapAdjacences.get(nodeA).add(nodeB);
	}
	
	public void addLink(Node<T> nodeA, Node<T> nodeB) {
		if(mapAdjacences.containsKey(nodeA)) {
			mapAdjacences.get(nodeA).add(nodeB);
		}else {
			mapAdjacences.put(nodeA, new HashSet<Node<T>>(Arrays.asList(nodeB)));
		}
	}

	public Node<T> getNodeByValue(T value) {
		for (Node<T> node : this.mapAdjacences.keySet()) {
			if (node.getValue().equals(value)) {
				return node;
			}
		}
		return null;
	}

	public Set<Node<T>> getNodes() {
		return this.mapAdjacences.keySet();
	}

	public Set<Node<T>> getAdjacences(Node<T> node) {
		return mapAdjacences.get(node);
	}

	public boolean isEmpty() {
		return mapAdjacences.isEmpty();
	}

	public Set<Node<T>> getLeafs() {
		Set<Node<T>> leafs = new HashSet<Node<T>>();
		if(!mapAdjacences.isEmpty()) {
			int maxIncidency = 0;
			do {
				for(Node<T> node : mapAdjacences.keySet()) {
					if(mapAdjacences.get(node).size() == maxIncidency) {
						leafs.add(node);
					}
				}
				maxIncidency++;
			}while(leafs.isEmpty());
		}
		return leafs;
	}

	public Set<T> removeLeafs() {
		Set<T> leafs = new HashSet<T>();
		for(Node<T> node : getLeafs()) {
			mapAdjacences.remove(node);
			for(Node<T> otherNode : mapAdjacences.keySet()) {
				if(mapAdjacences.get(otherNode).contains(node)) {
					mapAdjacences.get(otherNode).remove(node);
				}
			}
			leafs.add(node.getValue());
		}
		return leafs;
	}
	
	public SimpleGraph<T> merge(SimpleGraph<T> mergeFromGraph) {
		for(Node<T> node : mergeFromGraph.mapAdjacences.keySet()) {
			if(!mapAdjacences.containsKey(node)) {
				mapAdjacences.put(node, new HashSet<Node<T>>());
			}
			
			for(Node<T> neighbor : mergeFromGraph.mapAdjacences.get(node)) {
				this.addLink(node, neighbor);
			}
		}
		return this;
	}

	public void addNode(Node<T> node) {
		mapAdjacences.put(node, new HashSet<Node<T>>());
	}
	
}
