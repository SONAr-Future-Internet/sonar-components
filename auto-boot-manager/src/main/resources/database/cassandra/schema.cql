-- Topolgy
CREATE KEYSPACE IF NOT EXISTS topology WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy', 'datacenter1' : 3 };
CREATE TABLE IF NOT EXISTS topology.domain ( 
	idDomain UUID PRIMARY KEY, 
	name TEXT, 
	networkAddress TEXT, 
	networkMask TEXT, 
	ipRangeStart TEXT, 
	ipRangeFinish TEXT );
CREATE TABLE IF NOT EXISTS topology.element ( 
	idElement UUID PRIMARY KEY, 
	idDomain UUID, 
	name TEXT,
	typeElement TEXT,
	managementIPAddressList list<TEXT>,
	memory INT,
	cores INT,
	clock DECIMAL,
	disk BIGINT,
	cost DECIMAL,
	energy DECIMAL,
	lastDicoveredAt TIMESTAMP,
	lastDicoveredBy TEXT,
	lastDicoveredMethod TEXT,
	lastDicoveredSource TEXT );
CREATE TABLE IF NOT EXISTS topology.port ( 
	idPort UUID PRIMARY KEY,
	idElement UUID,
	remoteIdPort UUID,
	ifId TEXT,
	ifName TEXT,
	macAddress TEXT,
	ipAddress TEXT,
	remoteIfId TEXT,
	remoteName TEXT,
	remoteMacAddress TEXT,
	remoteIpAddress TEXT,
	mode TEXT,
	bandwidth BIGINT );
-- Property
CREATE KEYSPACE IF NOT EXISTS property WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy', 'datacenter1' : 3 };
CREATE TABLE IF NOT EXISTS property.configuration ( 
	group TEXT, 
	key TEXT, 
	value TEXT,
	PRIMARY KEY (group, key) );
CREATE TABLE IF NOT EXISTS property.data (
	application TEXT,
 	instance TEXT,
	group TEXT, 
	key TEXT, 
	value TEXT,
	PRIMARY KEY (application, instance, group, key) );