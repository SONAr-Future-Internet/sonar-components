package br.ufu.facom.mehar.sonar.collectors.metrics.model;

import java.io.Serializable;

public class Cluster implements Serializable {

	private static final long serialVersionUID = 1L;
	private Integer id;
	private Integer deviceCount;
	private Integer linkCount;
	private String root;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getDeviceCount() {
		return deviceCount;
	}

	public void setDeviceCount(Integer deviceCount) {
		this.deviceCount = deviceCount;
	}

	public Integer getLinkCount() {
		return linkCount;
	}

	public void setLinkCount(Integer linkCount) {
		this.linkCount = linkCount;
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{id: ");
		sb.append(id);
		sb.append(", deviceCount: ");
		sb.append(deviceCount);
		sb.append(", linkCount: ");
		sb.append(linkCount);
		sb.append(", root: ");
		sb.append(root);
		sb.append("}");
		return sb.toString();
	}
	
}