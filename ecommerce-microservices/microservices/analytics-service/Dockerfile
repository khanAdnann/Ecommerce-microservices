FROM openjdk:21-jre-slim

WORKDIR /app

COPY target/analytics-service-1.0.0.jar app.jar

EXPOSE 8089

ENTRYPOINT ["java", "-jar", "app.jar"]
