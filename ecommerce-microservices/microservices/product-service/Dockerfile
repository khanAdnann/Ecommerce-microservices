FROM openjdk:21-jre-slim

WORKDIR /app

COPY target/product-service-1.0.0.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
