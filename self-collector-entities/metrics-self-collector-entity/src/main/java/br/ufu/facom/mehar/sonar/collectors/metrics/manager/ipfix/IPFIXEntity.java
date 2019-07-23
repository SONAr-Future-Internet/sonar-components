package br.ufu.facom.mehar.sonar.collectors.metrics.manager.ipfix;

import br.ufu.facom.mehar.sonar.collectors.metrics.manager.util.HeaderBytesException;

public interface IPFIXEntity {
	public String toString();
	public byte[] getBytes() throws HeaderBytesException;
}