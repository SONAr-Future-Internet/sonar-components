package br.ufu.facom.mehar.sonar.organizing.configuration.algorithm.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.ufu.facom.mehar.sonar.core.util.Pair;

public class Path<T,U> {
	private Node<T> origin;
	private Map<Node<T>,List<Pair<Node<T>,Edge<U>>>> pathMap;
	private Map<Node<T>,Integer> distanceMap;
	
	public Path(Node<T> origin, Set<Node<T>> targetSet) {
		pathMap = new HashMap<Node<T>, List<Pair<Node<T>,Edge<U>>>>(targetSet.size());
		distanceMap = new HashMap<Node<T>, Integer>(targetSet.size());
		for(Node<T> node : targetSet) {
			pathMap.put(node, new LinkedList<Pair<Node<T>,Edge<U>>>());
			distanceMap.put(node, Integer.MAX_VALUE);
		}
		
		distanceMap.put(origin, 0);
	}

	public Node<T> getOrigin() {
		return origin;
	}

	public void setOrigin(Node<T> origin) {
		this.origin = origin;
	}

	public Map<Node<T>, List<Pair<Node<T>, Edge<U>>>> getPathMap() {
		return pathMap;
	}

	public void setPathMap(Map<Node<T>, List<Pair<Node<T>, Edge<U>>>> pathMap) {
		this.pathMap = pathMap;
	}

	public Map<Node<T>, Integer> getDistanceMap() {
		return distanceMap;
	}

	public void setDistanceMap(Map<Node<T>, Integer> distanceMap) {
		this.distanceMap = distanceMap;
	}

	public Integer getDistance(Node<T> node) {
		return this.getDistanceMap().get(node);
	}

	public void setDistance(Node<T> node, Integer distance) {
		this.getDistanceMap().put(node, distance);
	}

	public List<Pair<Node<T>, Edge<U>>> getPath(Node<T> node) {
		return this.getPathMap().get(node);
	}
	
	public List<Pair<Node<T>, Edge<U>>> getPath(T value) {
		return this.getPathMap().get(this.getNode(value));
	}
	
	public void setPath(Node<T> node, List<Pair<Node<T>, Edge<U>>> path) {
		this.getPathMap().put(node, path);
	}
	
	public Node<T> getNode(T value) {
		for(Node<T> node : this.getPathMap().keySet()) {
			if(node.getValue().equals(value)) {
				return node;
			}
		}
		return null;
	}
}
