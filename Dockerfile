FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY job-service/pom.xml job-service/pom.xml
COPY scheduler-service/pom.xml scheduler-service/pom.xml
COPY executor-service/pom.xml executor-service/pom.xml
COPY gateway-service/pom.xml gateway-service/pom.xml

RUN mvn -B dependency:go-offline -pl gateway-service -am

COPY common common
COPY auth-service auth-service
COPY job-service job-service
COPY scheduler-service scheduler-service
COPY executor-service executor-service
COPY gateway-service gateway-service

ARG MODULE=gateway-service
RUN mvn -B package -pl ${MODULE} -am -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
ARG MODULE=gateway-service
ARG JAR_FILE=target/${MODULE}-1.0.0-SNAPSHOT.jar
COPY --from=build /app/${MODULE}/${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
