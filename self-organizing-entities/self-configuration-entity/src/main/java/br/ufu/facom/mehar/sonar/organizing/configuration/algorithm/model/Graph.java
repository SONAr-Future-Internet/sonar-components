package br.ufu.facom.mehar.sonar.organizing.configuration.algorithm.model;

import java.util.HashMap;
import java.util.Map;

import br.ufu.facom.mehar.sonar.core.util.Pair;

public class Graph<T, U> {
	private Map<Node<T>, Map<Node<T>, Edge<U>>> mapAdjacences = new HashMap<Node<T>, Map<Node<T>, Edge<U>>>();

	public void addLink(T valueA, T valueB, Pair<U, U> link) {
		this.addLink(valueA, valueB, link, Boolean.TRUE);
	}

	public void addLink(T valueA, T valueB, Pair<U, U> link, Boolean bidirectional) {
		Node<T> nodeA = getNodeByValue(valueA);
		if (nodeA == null) {
			nodeA = new Node<T>(valueA);
			mapAdjacences.put(nodeA, new HashMap<Node<T>, Edge<U>>());
		}

		Node<T> nodeB = getNodeByValue(valueB);
		if (nodeB == null) {
			nodeB = new Node<T>(valueB);
			mapAdjacences.put(nodeB, new HashMap<Node<T>, Edge<U>>());
		}

		mapAdjacences.get(nodeA).put(nodeB, new Edge<U>(link.getFirst(), link.getSecond()));
		if (bidirectional) {
			mapAdjacences.get(nodeB).put(nodeA, new Edge<U>(link.getSecond(), link.getFirst()));
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

	public Map<Node<T>, Map<Node<T>, Edge<U>>> getMapAdjacences() {
		return mapAdjacences;
	}

	public void setMapAdjacences(Map<Node<T>, Map<Node<T>, Edge<U>>> mapAdjacences) {
		this.mapAdjacences = mapAdjacences;
	}
}
