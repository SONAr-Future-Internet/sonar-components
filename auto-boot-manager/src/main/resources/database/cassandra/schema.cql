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
	lastDicoveredAt TEXT,
	lastDicoveredBy TEXT,
	lastDicoveredMethod TEXT,
	lastDicoveredSource TEXT,
	ofControllers list<TEXT>,
	ofDeviceId TEXT,
	manufacturer TEXT,
	product TEXT,
	software TEXT,
	state TEXT,
	discoveryFailureCount INT,
	topologyLevel INT );
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