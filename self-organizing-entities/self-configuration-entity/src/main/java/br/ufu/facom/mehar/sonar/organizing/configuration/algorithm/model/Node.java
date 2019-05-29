package br.ufu.facom.mehar.sonar.organizing.configuration.algorithm.model;

public class Node<T> {
	private T value;

	public Node(T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	public void setName(T value) {
		this.value = value;
	}
}