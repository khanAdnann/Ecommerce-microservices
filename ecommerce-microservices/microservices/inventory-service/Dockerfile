FROM openjdk:21-jre-slim

WORKDIR /app

COPY target/inventory-service-1.0.0.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app.jar"]
