FROM openjdk:17
COPY product-service-0.0.1-SNAPSHOT.jar /app/auth-service.jar
CMD ["java", "-jar", "app/auth-service.jar"]
