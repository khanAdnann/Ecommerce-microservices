FROM openjdk:21-jre-slim

WORKDIR /app

COPY target/order-service-1.0.0.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
