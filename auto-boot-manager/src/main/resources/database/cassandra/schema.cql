-- Core
CREATE KEYSPACE IF NOT EXISTS core WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1  };
CREATE TABLE IF NOT EXISTS core.controller (
	idController UUID PRIMARY KEY,  
	north TEXT, 
	south TEXT, 
	interceptor TEXT,
	strategy TEXT,
	authUsername TEXT,
	authPassword TEXT );
-- Topolgy
CREATE KEYSPACE IF NOT EXISTS topology WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
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
	ipAddressList list<TEXT>,
	memory INT,
	cores INT,
	clock DECIMAL,
	disk BIGINT,
	cost DECIMAL,
	energy DECIMAL,
	lastDicoveredAt TEXT,
	lastDicoveredBy TEXT,
	lastDicoveredMethod TEXT,
	lastDicoveredSource TEXT,
	ofControllerList list<UUID>,
	ofDeviceId TEXT,
	ofChannel TEXT,
	manufacturer TEXT,
	product TEXT,
	software TEXT,
	state TEXT,
	discoveryFailureCount INT );
CREATE TABLE IF NOT EXISTS topology.port ( 
	idPort UUID PRIMARY KEY,
	idElement UUID,
	remoteIdPort UUID,
	portId TEXT,
	portName TEXT,
	macAddress TEXT,
	ipAddress TEXT,
	remotePortId TEXT,
	remotePortName TEXT,
	remoteHostName TEXT,
	remoteMacAddress TEXT,
	remoteIpAddress TEXT,
	mode TEXT,
	speed INT,
	ofPort TEXT,
	state TEXT,
	adminState TEXT );
-- Property
CREATE KEYSPACE IF NOT EXISTS property WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
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