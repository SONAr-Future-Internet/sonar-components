#!/bin/bash
 
case ${1,,} in
	ascoe) 	target="self-collector-entities/alarms-self-collector-entity" ;;
	mscoe) 	target="self-collector-entities/metrics-self-collector-entity" ;;
	sscoe) 	target="self-collector-entities/samples-self-collector-entity" ;;
	mscoe) 	target="self-collector-entities/topology-self-collector-entity" ;;
	lscoe) 	target="self-collector-entities/logs-self-collector-entity" ;;
	dsle) 	target="self-learning-entities/diagnosis-self-learning-entity" ;;
	rsle) 	target="self-learning-entities/rating-self-learning-entity" ;;
	isle) 	target="self-learning-entities/intent-self-learning-entity" ;;
	tsle) 	target="self-learning-entities/tunning-self-learning-entity" ;;
	psle) 	target="self-learning-entities/prediction-self-learning-entity" ;;
	sce)  	target="self-organizing-entities/self-configuration-entity" ;;
	she)  	target="self-organizing-entities/self-healing-entity" ;;
	sme)  	target="self-organizing-entities/self-management-entity" ;;
	soe)  	target="self-organizing-entities/self-optimization-entity" ;;
	sple)  	target="self-organizing-entities/self-planning-entity" ;;
	spe)  	target="self-organizing-entities/self-protection-entity" ;;
	asb)  	target="sonar-service-broker/administration-service-broker" ;;
	csb)  	target="sonar-service-broker/customer-service-broker" ;;
	cpi)	target="function-apps/control-plane-interceptor" ;;
	cim)	target="custom-components/sonar-containerized-infrastructure-manager" ;;
	dhcp)	target="custom-components/sonar-dhcp-server" ;;
	abm)  	target="auto-boot-manager" ;;
	sd)   	target="sonar-dashboard" ;;

	alarms-self-collector-entity) 				target="self-collector-entities/alarms-self-collector-entity" ;;
	metrics-self-collector-entity) 				target="self-collector-entities/metrics-self-collector-entity" ;;
	samples-self-collector-entity) 				target="self-collector-entities/samples-self-collector-entity" ;;
	topology-self-collector-entity) 			target="self-collector-entities/topology-self-collector-entity" ;;
	logs-self-collector-entity) 				target="self-collector-entities/logs-self-collector-entity" ;;
	diagnosis-self-learning-entity) 			target="self-learning-entities/diagnosis-self-learning-entity" ;;
	rating-self-learning-entity) 				target="self-learning-entities/rating-self-learning-entity" ;;
	intent-self-learning-entity) 				target="self-learning-entities/intent-self-learning-entity" ;;
	tunning-self-learning-entity) 				target="self-learning-entities/tunning-self-learning-entity" ;;
	prediction-self-learning-entity) 			target="self-learning-entities/prediction-self-learning-entity" ;;
	self-configuration-entity)  				target="self-organizing-entities/self-configuration-entity" ;;
	self-healing-entity)  						target="self-organizing-entities/self-healing-entity" ;;
	self-management-entity)  					target="self-organizing-entities/self-management-entity" ;;
	self-optimization-entity)  					target="self-organizing-entities/self-optimization-entity" ;;
	self-planning-entity)  						target="self-organizing-entities/self-planning-entity" ;;
	self-protection-entity)  					target="self-organizing-entities/self-protection-entity" ;;
	administration-service-broker)  			target="sonar-service-broker/administration-service-broker" ;;
	customer-service-broker)  					target="sonar-service-broker/customer-service-broker" ;;
	control-plance-interceptor)					target="function-apps/control-plane-interceptor" ;;
	containerized-infrastructure-manager)		target="custom-components/sonar-containerized-infrastructure-manager" ;;
	dhcp-server)								target="custom-components/sonar-dhcp-server" ;;
	auto-boot-manager)  						target="auto-boot-manager" ;;
	sonar-dashboard)   							target="sonar-dashboard" ;;

	alarms-collector) 		target="self-collector-entities/alarms-self-collector-entity" ;;
	metrics-collector) 		target="self-collector-entities/metrics-self-collector-entity" ;;
	samples-collector) 		target="self-collector-entities/samples-self-collector-entity" ;;
	topology-collector) 	target="self-collector-entities/topology-self-collector-entity" ;;
	logs-collector) 		target="self-collector-entities/logs-self-collector-entity" ;;
	diagnosis-learning) 	target="self-learning-entities/diagnosis-self-learning-entity" ;;
	rating-learning) 		target="self-learning-entities/rating-self-learning-entity" ;;
	intent-learning) 		target="self-learning-entities/intent-self-learning-entity" ;;
	tunning-learning) 		target="self-learning-entities/tunning-self-learning-entity" ;;
	prediction-learning) 	target="self-learning-entities/prediction-self-learning-entity" ;;
	configuration-entity) 	target="self-organizing-entities/self-configuration-entity" ;;
	healing-entity)  		target="self-organizing-entities/self-healing-entity" ;;
	management-entity)  	target="self-organizing-entities/self-management-entity" ;;
	optimization-entity)  	target="self-organizing-entities/self-optimization-entity" ;;
	planning-entity)  		target="self-organizing-entities/self-planning-entity" ;;
	protection-entity)  	target="self-organizing-entities/self-protection-entity" ;;
	administration-broker)  target="sonar-service-broker/administration-service-broker" ;;
	customer-broker)  		target="sonar-service-broker/customer-service-broker" ;;
	interceptor)			target="function-apps/control-plane-interceptor" ;;
	container-manager)		target="custom-components/sonar-containerized-infrastructure-manager" ;;
	boot-manager)  			target="auto-boot-manager" ;;
	dashboard)   			target="sonar-dashboard" ;;

	alarms) 		target="self-collector-entities/alarms-self-collector-entity" ;;
	metrics) 		target="self-collector-entities/metrics-self-collector-entity" ;;
	samples) 		target="self-collector-entities/samples-self-collector-entity" ;;
	topology) 		target="self-collector-entities/topology-self-collector-entity" ;;
	logs) 			target="self-collector-entities/logs-self-collector-entity" ;;
	diagnosis) 		target="self-learning-entities/diagnosis-self-learning-entity" ;;
	rating) 		target="self-learning-entities/rating-self-learning-entity" ;;
	intent) 		target="self-learning-entities/intent-self-learning-entity" ;;
	tunning) 		target="self-learning-entities/tunning-self-learning-entity" ;;
	prediction) 	target="self-learning-entities/prediction-self-learning-entity" ;;
	configuration) 	target="self-organizing-entities/self-configuration-entity" ;;
	healing)       	target="self-organizing-entities/self-healing-entity" ;;
	management)    	target="self-organizing-entities/self-management-entity" ;;
	optimization)  	target="self-organizing-entities/self-optimization-entity" ;;
	planning)      	target="self-organizing-entities/self-planning-entity" ;;
	protection)    	target="self-organizing-entities/self-protection-entity" ;;

	h) ;;
	help) ;;

	*) echo "Invalid Option!" ;;
esac

if [ 	-z "$target" ]
  then
    echo -e "Usage:"
	echo -e "\t./run.sh ascoe|alarms|alarms-collector|alarms-self-collector-entity ---------- runs 'Alarms Self-Collector Entity' application."
	echo -e "\t./run.sh mscoe|metrics|metrics-collector|metrics-self-collector-entity ------- runs 'Metrics Self-Collector Entity' application."
	echo -e "\t./run.sh sscoe|samples|samples-collector|samples-self-collector-entity ------- runs 'Samples Self-Collector Entity' application."
	echo -e "\t./run.sh tscoe|toplogy|topology-collector|topology-self-collector-entity ----- runs 'Topology Self-Collector Entity' application."
	echo -e "\t./run.sh lscoe|logs|logs-collector|logs-self-collector-entity ---------------- runs 'Logs Self-Collector Entity' application."
	echo -e "\t./run.sh dsle|diagnosis|diagnosis-learning|diagnosis-self-learning-entity ---- runs 'Diagnosis Self-Learning Entity' application."
	echo -e "\t./run.sh tsle|tunning|tunning-learning|tunning-self-learning-entity ---------- runs 'Tunning Self-Learning Entity' application."
	echo -e "\t./run.sh psle|prediction|prediction-learning|prediction-self-learning-entity - runs 'Prediction Self-Learning Entity' application."
	echo -e "\t./run.sh rsle|rating|rating-learning|rating-self-learning-entity ------------- runs 'Rating Self-Learning Entity' application."
	echo -e "\t./run.sh isle|intent|intent-learning|intent-self-learning-entity ------------- runs 'Intent Self-Learning Entity' application."
	echo -e "\t./run.sh sce|configuration|configuration-entity|self-configuration-entity ---- runs 'Self-Configuration Entity' application."
	echo -e "\t./run.sh she|healing|healing-entity|self-healing-entity ---------------------- runs 'Self-Healing Entity' application."
	echo -e "\t./run.sh sme|management|management-entity|self-management-entity ------------- runs 'Self-Management Entity' application."
	echo -e "\t./run.sh soe|optimization|optimization-entity|self-optimization-entity ------- runs 'Self-Optimization Entity' application."
	echo -e "\t./run.sh sple|planning|planning-entity|self-planning-entity ------------------ runs 'Self-Planning Entity' application."
	echo -e "\t./run.sh spe|protection|protection-entity|self-protection-entity ------------- runs 'Self-Protection Entity' application."
	echo -e "\t./run.sh asb|administration-bus|administration-service-bus ------------------- runs 'Administration-Service-Broker' application."
	echo -e "\t./run.sh csb|customer-roker|customer-service-broker -------------------------- runs 'Customer-Service-Broker' application."
	echo -e "\t./run.sh abm|boot-manager|auto-boot-manager ---------------------------------- runs 'Auto-Boot-Manager' application."
	echo -e "\t./run.sh cpi|interceptor|control-plane-interceptor --------------------------- runs 'Control-Plane-Interceptor' application."
	echo -e "\t./run.sh cim|containerized-infrastructure-manager|container-manager ---------- runs 'SONAr-Containerized-Infrastructure-Manager' application."
	echo -e "\t./run.sh dhcp|dhcp-server ---------------------------------------------------- runs 'SONAr-DHCP-Server' application."
	echo -e "\t./run.sh h|help -------------------------------------------------------------- shows the information above."
else
     mvn spring-boot:run -pl $target
fi
