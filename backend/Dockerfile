# Stage 1: Build the application using Maven
FROM maven:3.8.4-openjdk-17-slim AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and src folder to the container
COPY pom.xml /app/pom.xml
COPY src /app/src

# Package the Spring Boot application
RUN mvn clean package -DskipTests

# Stage 2: Run the application in a minimal JDK image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /app/target/*.jar /app/inventory-management-server.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/inventory-management-server.jar"]
