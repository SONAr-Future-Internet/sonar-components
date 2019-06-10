package br.ufu.facom.mehar.sonar.organizing.configuration.algorithm.model;

public class Edge<U> {
	private U peerA;
	private U peerB;
	
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((peerA == null) ? 0 : peerA.hashCode());
		result = prime * result + ((peerB == null) ? 0 : peerB.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Edge<U> other = (Edge<U>) obj;
		if (peerA == null) {
			if (other.peerA != null)
				return false;
		} else if (!peerA.equals(other.peerA))
			return false;
		if (peerB == null) {
			if (other.peerB != null)
				return false;
		} else if (!peerB.equals(other.peerB))
			return false;
		return true;
	}
	
}
