package br.ufu.facom.mehar.sonar.client.ndb.repository;

public interface DatabaseBuilder {
	void buildOrAlter();
	Boolean isBuilt();
}
