package br.ufu.facom.mehar.sonar.client.dndb.repository;

public interface DatabaseBuilder {
	void buildOrAlter();
	Boolean isBuilt();
}
