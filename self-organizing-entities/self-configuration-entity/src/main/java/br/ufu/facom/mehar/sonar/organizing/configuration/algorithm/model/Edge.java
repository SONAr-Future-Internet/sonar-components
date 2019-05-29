package br.ufu.facom.mehar.sonar.organizing.configuration.algorithm.model;

public class Edge<U> {
	U peerA;
	U peerB;
	
	public Edge(U peerA, U peerB) {
		this.peerA = peerA;
		this.peerB = peerB;
	}

	public int getWeight() {
		return 1;
	}

	public U getPeerA() {
		return peerA;
	}

	public void setPeerA(U peerA) {
		this.peerA = peerA;
	}

	public U getPeerB() {
		return peerB;
	}

	public void setPeerB(U peerB) {
		this.peerB = peerB;
	}
}
