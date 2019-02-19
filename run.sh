#!/bin/bash
 
case ${1,,} in
	nem)  target="network-event-manager" ;;
	nsb)  target="network-service-bus" ;;
	asce) target="self-collector-entities/alarms-self-collector-entity" ;;
	msce) target="self-collector-entities/metrics-self-collector-entity" ;;
	ssce) target="self-collector-entities/samples-self-collector-entity" ;;
	msce) target="self-collector-entities/topology-self-collector-entity" ;;
	asle) target="self-learning-entities/analysis-self-learning-entity" ;;
	csle) target="self-learning-entities/configuration-self-learning-entity" ;;
	psle) target="self-learning-entities/prediction-self-learning-entity" ;;
	sce)  target="self-organizing-entities/self-configuration-entity" ;;
	she)  target="self-organizing-entities/self-healing-entity" ;;
	sme)  target="self-organizing-entities/self-management-entity" ;;
	soe)  target="self-organizing-entities/self-optimization-entity" ;;
	spe)  target="self-organizing-entities/self-planning-entity" ;;
	sse)  target="self-organizing-entities/self-protection-entity" ;;
	sd)   target="sonar-dashboard" ;;

	network-event-manager)              target="network-event-manager" ;;
	network-service-bus)                target="network-service-bus" ;;
	alarms-self-collector-entity)       target="self-collector-entities/alarms-self-collector-entity" ;;
	metrics-self-collector-entity)      target="self-collector-entities/metrics-self-collector-entity" ;;
	samples-self-collector-entity)      target="self-collector-entities/samples-self-collector-entity" ;;
	topology-self-collector-entity)     target="self-collector-entities/topology-self-collector-entity" ;;
	analysis-self-learning-entity)      target="self-learning-entities/analysis-self-learning-entity" ;;
	configuration-self-learning-entity) target="self-learning-entities/configuration-self-learning-entity" ;;
	prediction-self-learning-entity)    target="self-learning-entities/prediction-self-learning-entity" ;;
	self-configuration-entity)          target="self-organizing-entities/self-configuration-entity" ;;
	self-healing-entity)                target="self-organizing-entities/self-healing-entity" ;;
	self-management-entity)             target="self-organizing-entities/self-management-entity" ;;
	self-optimization-entity)           target="self-organizing-entities/self-optimization-entity" ;;
	self-planning-entity)               target="self-organizing-entities/self-planning-entity" ;;
	self-protection-entity)             target="self-organizing-entities/self-protection-entity" ;;
	sonar-dashboard)                    target="sonar-dashboard" ;;

	event-manager)          target="network-event-manager" ;;
	service-bus)            target="network-service-bus" ;;
	alarms-collector)       target="self-collector-entities/alarms-self-collector-entity" ;;
	metrics-collector)      target="self-collector-entities/metrics-self-collector-entity" ;;
	samples-collector)      target="self-collector-entities/samples-self-collector-entity" ;;
	topology-collector)     target="self-collector-entities/topology-self-collector-entity" ;;
	analysis-learning)      target="self-learning-entities/analysis-self-learning-entity" ;;
	configuration-learning) target="self-learning-entities/configuration-self-learning-entity" ;;
	prediction-learning)    target="self-learning-entities/prediction-self-learning-entity" ;;
	configuration-entity)   target="self-organizing-entities/self-configuration-entity" ;;
	healing-entity)         target="self-organizing-entities/self-healing-entity" ;;
	management-entity)      target="self-organizing-entities/self-management-entity" ;;
	optimization-entity)    target="self-organizing-entities/self-optimization-entity" ;;
	planning-entity)        target="self-organizing-entities/self-planning-entity" ;;
	protection-entity)      target="self-organizing-entities/self-protection-entity" ;;
	dashboard)              target="sonar-dashboard" ;;

	configuration) target="self-organizing-entities/self-configuration-entity" ;;
	healing)       target="self-organizing-entities/self-healing-entity" ;;
	management)    target="self-organizing-entities/self-management-entity" ;;
	optimization)  target="self-organizing-entities/self-optimization-entity" ;;
	planning)      target="self-organizing-entities/self-planning-entity" ;;
	protection)    target="self-organizing-entities/self-protection-entity" ;;

	h) ;;
	help) ;;

	*) echo "Invalid Option!" ;;
esac

if [ 	-z "$target" ]
  then
     echo -e "Usage:"
     echo -e "\t./run.sh nem|event-manager|network-event-manager -------------------------- runs 'Network-Event-Manager' application."
     echo -e "\t./run.sh nsb|service-bus|network-service-bus ------------------------------ runs 'Network-Service-Manager' application."
     echo -e "\t./run.sh asce|alarms-collector|alarms-self-collector-entity --------------- runs 'Alarms Self-Collector Entity' application."
     echo -e "\t./run.sh msce|metrics-collector|metrics-self-collector-entity ------------- runs 'Metrics Self-Collector Entity' application."
     echo -e "\t./run.sh ssce|samples-collector|samples-self-collector-entity ------------- runs 'Samples Self-Collector Entity' application."
     echo -e "\t./run.sh tsce|topology-collector|topology-self-collector-entity ----------- runs 'Topology Self-Collector Entity' application."
     echo -e "\t./run.sh asle|analysis-learning|analysis-self-learning-entity ------------- runs 'Analysis Self-Learning Entity' application."
     echo -e "\t./run.sh csle|configuration-learning|configuration-self-learning-entity --- runs 'Configuration Self-Learning Entity' application."
     echo -e "\t./run.sh psle|prediction-learning|prediction-self-learning-entity --------- runs 'Prediction Self-Learning Entity' application."
     echo -e "\t./run.sh sce|configuration|configuration-entity|self-configuration-entity - runs 'Self-Configuration Entity' application."
     echo -e "\t./run.sh she|healing|healing-entity|self-healing-entity ------------------- runs 'Self-Healing Entity' application."
     echo -e "\t./run.sh sme|management|management-entity|self-management-entity ---------- runs 'Self-Management Entity' application."
     echo -e "\t./run.sh soe|optimization|optimization-entity|self-optimization-entity ---- runs 'Self-Optimization Entity' application."
     echo -e "\t./run.sh spe|planning|planning-entity|self-planning-entity ---------------- runs 'Self-Planning Entity' application."
     echo -e "\t./run.sh sse|protection|protection-entity|self-protection-entity ---------- runs 'Self-Protection Entity' application."
     echo -e "\t./run.sh h|help ----------------------------------------------------------- shows the information above."
else
     mvn spring-boot:run -pl $target
fi
