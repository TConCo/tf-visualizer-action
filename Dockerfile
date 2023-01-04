FROM openjdk:11
MAINTAINER fatihtokus
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]