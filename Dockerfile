FROM openjdk:11 as builder
COPY ./pom.xml ./pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY ./src ./src
RUN ["chmod", "+x", "mvnw"]
RUN ./mvnw dependency:go-offline -B
RUN ./mvnw clean package -q && cp target/*.jar app.jar
COPY app.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]