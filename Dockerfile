# Use the official OpenJDK 11 base image
#FROM openjdk:11-jre-slim

# Set the working directory inside the container
#WORKDIR /app

# Copy the application JAR file from the target directory into the container
#COPY target/rest-service-complete-0.0.1-SNAPSHOT.jar app.jar

# Define the command to run the application
#ENTRYPOINT ["java", "-jar", "/app.jar"]
FROM openjdk:17-jdk-slim
COPY target/rest-service-complete-0.0.1-SNAPSHOT.jar /app.jar
COPY application.properties /app/config/application.properties
# Set the Spring configuration location
ENV SPRING_CONFIG_LOCATION=file:/app/config/application.properties
ENTRYPOINT ["java", "-jar", "/app.jar"]
