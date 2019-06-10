package br.ufu.facom.mehar.sonar.organizing.configuration.algorithm.model;

import java.util.Map;

public class TreeNode<T,U> {
	private T value;
	private int level;
	
	private TreeNode<T, U> parent;
	private Map<U,TreeNode<T,U>> children;

	public TreeNode(T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	public void setName(T value) {
		this.value = value;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public Map<U, TreeNode<T, U>> getChildren() {
		return children;
	}

	public void setChildren(Map<U, TreeNode<T, U>> children) {
		this.children = children;
	}

	public void setValue(T value) {
		this.value = value;
	}
	
	public boolean isRoot() {
		return this.parent == null;
	}
	
	public boolean isLeaf() {
		return this.children.isEmpty();
	}
}