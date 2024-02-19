#FROM maven:3.8.1-openjdk-17-slim AS build
#COPY src /home/app/src
#COPY pom.xml /home/app
#RUN mvn -P k8s -f /home/app/pom.xml clean package -DskipTests=true

#FROM openjdk:17-alpine
#COPY --from=build /home/app/target/*.jar app.jar
#ENTRYPOINT ["java","-jar","/app.jar"]

FROM openjdk:17
VOLUME /tmp
EXPOSE 8080
ARG JAR_FILE=target/demo-service-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]