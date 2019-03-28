#!/bin/bash
 
if [ ! -z "$1" ]
	then

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
		asb)  	target="sonar-service-bus/administration-service-bus" ;;
		csb)  	target="sonar-service-bus/customer-service-bus" ;;
		isb)  	target="sonar-service-bus/integration-service-bus" ;;
		nem)  	target="network-event-manager" ;;
		abm)  	target="auto-boot-manager" ;;
		cpi)  	target="control-plane-interceptor" ;;
		sd)   	target="sonar-dashboard" ;;

		alarms-self-collector-entity) 		target="self-collector-entities/alarms-self-collector-entity" ;;
		metrics-self-collector-entity) 		target="self-collector-entities/metrics-self-collector-entity" ;;
		samples-self-collector-entity) 		target="self-collector-entities/samples-self-collector-entity" ;;
		topology-self-collector-entity) 	target="self-collector-entities/topology-self-collector-entity" ;;
		logs-self-collector-entity) 		target="self-collector-entities/logs-self-collector-entity" ;;
		diagnosis-self-learning-entity) 	target="self-learning-entities/diagnosis-self-learning-entity" ;;
		rating-self-learning-entity) 		target="self-learning-entities/rating-self-learning-entity" ;;
		intent-self-learning-entity) 		target="self-learning-entities/intent-self-learning-entity" ;;
		tunning-self-learning-entity) 		target="self-learning-entities/tunning-self-learning-entity" ;;
		prediction-self-learning-entity) 	target="self-learning-entities/prediction-self-learning-entity" ;;
		self-configuration-entity)  		target="self-organizing-entities/self-configuration-entity" ;;
		self-healing-entity)  				target="self-organizing-entities/self-healing-entity" ;;
		self-management-entity)  			target="self-organizing-entities/self-management-entity" ;;
		self-optimization-entity)  			target="self-organizing-entities/self-optimization-entity" ;;
		self-planning-entity)  				target="self-organizing-entities/self-planning-entity" ;;
		self-protection-entity)  			target="self-organizing-entities/self-protection-entity" ;;
		administration-service-bus)  		target="sonar-service-bus/administration-service-bus" ;;
		customer-service-bus)  				target="sonar-service-bus/customer-service-bus" ;;
		integration-service-bus)  			target="sonar-service-bus/integration-service-bus" ;;
		network-event-manager)  			target="network-event-manager" ;;
		auto-boot-manager)  				target="auto-boot-manager" ;;
		control-plane-interceptor)  		target="control-plane-interceptor" ;;
		sonar-dashboard)   					target="sonar-dashboard" ;;

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
		configuration-entity)  	target="self-organizing-entities/self-configuration-entity" ;;
		healing-entity)  		target="self-organizing-entities/self-healing-entity" ;;
		management-entity)  	target="self-organizing-entities/self-management-entity" ;;
		optimization-entity)  	target="self-organizing-entities/self-optimization-entity" ;;
		planning-entity)  		target="self-organizing-entities/self-planning-entity" ;;
		protection-entity)  	target="self-organizing-entities/self-protection-entity" ;;
		administration-bus)  	target="sonar-service-bus/administration-service-bus" ;;
		customer-bus)  			target="sonar-service-bus/customer-service-bus" ;;
		integration-bus)  		target="sonar-service-bus/integration-service-bus" ;;
		event-manager)  		target="network-event-manager" ;;
		boot-manager)  			target="auto-boot-manager" ;;
		interceptor)  			target="control-plane-interceptor" ;;
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

		h)      showHelp=true ;;
		help)   showHelp=true ;;

		*) echo "Invalid Option!"; showHelp=true ;;
	esac
else
	buildAll=true
fi

if [ -z "$target" ]
  then
  	if [ ! -z "$showHelp" ]
  		then
  		 echo -e "Usage:"
  		 echo -e "\t./build.sh -------------------------------------------------------------------------- builds all modules."
	     echo -e "\t./build.sh ascoe|alarms|alarms-collector|alarms-self-collector-entity --------------- builds 'Alarms Self-Collector Entity' application."
	     echo -e "\t./build.sh mscoe|metrics|metrics-collector|metrics-self-collector-entity ------------ builds 'Metrics Self-Collector Entity' application."
	     echo -e "\t./build.sh sscoe|samples|samples-collector|samples-self-collector-entity ------------ builds 'Samples Self-Collector Entity' application."
	     echo -e "\t./build.sh tscoe|toplogy|topology-collector|topology-self-collector-entity ---------- builds 'Topology Self-Collector Entity' application."
	     echo -e "\t./build.sh lscoe|logs|logs-collector|logs-self-collector-entity --------------------- builds 'Logs Self-Collector Entity' application."
	     echo -e "\t./build.sh dsle|diagnosis|diagnosis-learning|diagnosis-self-learning-entity --------- builds 'Diagnosis Self-Learning Entity' application."
	     echo -e "\t./build.sh tsle|tunning|tunning-learning|tunning-self-learning-entity --------------- builds 'Tunning Self-Learning Entity' application."
	     echo -e "\t./build.sh psle|prediction|prediction-learning|prediction-self-learning-entity ------ builds 'Prediction Self-Learning Entity' application."
	     echo -e "\t./build.sh rsle|rating|rating-learning|rating-self-learning-entity ------------------ builds 'Rating Self-Learning Entity' application."
	     echo -e "\t./build.sh isle|intent|intent-learning|intent-self-learning-entity ------------------ builds 'Intent Self-Learning Entity' application."
	     echo -e "\t./build.sh sce|configuration|configuration-entity|self-configuration-entity --------- builds 'Self-Configuration Entity' application."
	     echo -e "\t./build.sh she|healing|healing-entity|self-healing-entity --------------------------- builds 'Self-Healing Entity' application."
	     echo -e "\t./build.sh sme|management|management-entity|self-management-entity ------------------ builds 'Self-Management Entity' application."
	     echo -e "\t./build.sh soe|optimization|optimization-entity|self-optimization-entity ------------ builds 'Self-Optimization Entity' application."
	     echo -e "\t./build.sh sple|planning|planning-entity|self-planning-entity ----------------------- builds 'Self-Planning Entity' application."
	     echo -e "\t./build.sh spe|protection|protection-entity|self-protection-entity ------------------ builds 'Self-Protection Entity' application."
	     echo -e "\t./build.sh asb|administration-bus|administration-service-bus ------------------------ builds 'Administration-Service-Manager' application."
	     echo -e "\t./build.sh csb|customer-bus|customer-service-bus ------------------------------------ builds 'Customer-Service-Manager' application."
	     echo -e "\t./build.sh isb|integration-bus|integration-service-bus ------------------------------ builds 'Integration-Service-Manager' application."
	     echo -e "\t./build.sh nem|event-manager|network-event-manager ---------------------------------- builds 'Network-Event-Manager' application."
	     echo -e "\t./build.sh abm|boot-manager|auto-boot-manager --------------------------------------- builds 'Auto-Boot-Manager' application."
	     echo -e "\t./build.sh cpi|interceptor|control-plane-interceptor -------------------------------- builds 'Control-Plane-Interceptor' application."
	     echo -e "\t./build.sh h|help ------------------------------------------------------------------- shows the information above."
  	fi

  	if [ ! -z "$buildAll" ]
  		then
  		mvn clean package -DskipTests   
  	fi
else
     mvn clean package -pl $target
fi
