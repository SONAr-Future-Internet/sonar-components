#Based on custom node
FROM meharsonar/node

#Support
MAINTAINER Mauricio Amaral Gonçalves <mauricioamaralg@ufu.br>

#Command: based on
#https://github.com/spotify/dockerfile-maven and 
#https://spring.io/guides/gs/spring-boot-docker/
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/usr/share/myservice/myservice.jar"]

# We added a VOLUME pointing to "/tmp" because that is where a Spring Boot application creates working directories for Tomcat by default.
VOLUME /tmp

# Add the service itself
ARG artifact
ADD target/${artifact} /usr/share/myservice/myservice.jar