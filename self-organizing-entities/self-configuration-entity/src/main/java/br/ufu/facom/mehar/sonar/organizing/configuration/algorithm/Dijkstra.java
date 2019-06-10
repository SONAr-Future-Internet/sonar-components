package br.ufu.facom.mehar.sonar.organizing.configuration.algorithm;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

import br.ufu.facom.mehar.sonar.organizing.configuration.algorithm.model.Graph;
import br.ufu.facom.mehar.sonar.core.util.Pair;
import br.ufu.facom.mehar.sonar.organizing.configuration.algorithm.model.Edge;
import br.ufu.facom.mehar.sonar.organizing.configuration.algorithm.model.Node;
import br.ufu.facom.mehar.sonar.organizing.configuration.algorithm.model.Path;

public class Dijkstra {
	
	public static <T,U> Path<T,U> calculateShortestPathFromSource(Graph<T,U> graph, T sourcevalue) {
		return calculateShortestPathFromSource(graph, graph.getNodeByValue(sourcevalue));
	}

	public static <T,U> Path<T,U> calculateShortestPathFromSource(Graph<T,U> graph, Node<T> source) {
		Path<T,U> shortestPath = new Path<T,U>(source, graph.getMapAdjacences().keySet());

		shortestPath.setOrigin(source);;
		
		Set<Node<T>> settledNodes = new HashSet<>();
		Set<Node<T>> unsettledNodes = new HashSet<>();

		unsettledNodes.add(source);

		while (unsettledNodes.size() != 0) {
			Node<T> currentNode = getLowestDistanceNode(shortestPath, unsettledNodes);
			unsettledNodes.remove(currentNode);
			for (Entry<Node<T>, Edge<U>> adjacencyPair : graph.getMapAdjacences().get(currentNode).entrySet()) {
				Node<T> adjacentNode = adjacencyPair.getKey();
				Edge<U> edge = adjacencyPair.getValue();
				if (!settledNodes.contains(adjacentNode)) {
					calculateMinimumDistance(shortestPath, adjacentNode, edge, currentNode);
					unsettledNodes.add(adjacentNode);
				}
			}
			settledNodes.add(currentNode);
		}
		return shortestPath;
	}

	private static <T,U> Node<T> getLowestDistanceNode(Path<T,U> shortestPath, Set<Node<T>> unsettledNodes) {
		Node<T> lowestDistanceNode = null;
		int lowestDistance = Integer.MAX_VALUE;
		for (Node<T> node : unsettledNodes) {
			int nodeDistance = shortestPath.getDistance(node);
			if (nodeDistance < lowestDistance) {
				lowestDistance = nodeDistance;
				lowestDistanceNode = node;
			}
		}
		return lowestDistanceNode;
	}

	private static <T,U> void calculateMinimumDistance(Path<T,U> shortestPath, Node<T> evaluationNode, Edge<U> edge, Node<T> sourceNode) {
		Integer sourceDistance = shortestPath.getDistance(sourceNode);
		if (sourceDistance + edge.getWeight() < shortestPath.getDistance(evaluationNode)) {
			shortestPath.setDistance(evaluationNode, sourceDistance + edge.getWeight());
			
			LinkedList<Pair<Node<T>,Edge<U>>> path = new LinkedList<>(shortestPath.getPath(sourceNode));
			path.add(new Pair<Node<T>, Edge<U>>(sourceNode, edge));
			shortestPath.setPath(evaluationNode, path);
		}
	}
}