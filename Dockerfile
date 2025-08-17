# Use a Java 21 base image
FROM eclipse-temurin:21-jdk-jammy

# Set working directory
WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn
COPY mvnw ./
COPY pom.xml ./

# Set execute permission for mvnw
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose the port your app runs on
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "target/*.jar", "--spring.profiles.active=prod"]
