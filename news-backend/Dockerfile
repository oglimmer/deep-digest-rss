# First stage: Build the application
FROM maven:3-eclipse-temurin-21 AS builder

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN MAVEN_OPTS=-XX:UseSVE=0 mvn dependency:go-offline

# Copy the source code and build the application
COPY src ./src
RUN MAVEN_OPTS=-XX:UseSVE=0 mvn package -DskipTests

# Second stage: Create the runtime image
FROM eclipse-temurin:25.0.1_8-jre-alpine

# Set the working directory in the container
WORKDIR /app

# Copy the executable JAR file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java", "-XX:UseSVE=0", "-jar", "app.jar"]
