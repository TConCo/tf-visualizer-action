FROM openjdk:11 as builder
COPY ./pom.xml ./pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY ./src ./src
RUN ["chmod", "+x", "mvnw"]
RUN ./mvnw dependency:go-offline -B
RUN ./mvnw clean package assembly:single -q && cp target/*jar-with-dependencies.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]