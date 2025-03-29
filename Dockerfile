FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/token-service-0.0.1-SNAPSHOT.jar token-service.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "token-service.jar"]