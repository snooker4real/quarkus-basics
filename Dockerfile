####
# This Dockerfile is used to build a Quarkus application for production deployment on Dokploy
####

## Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml first for better layer caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw package -DskipTests

## Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre

WORKDIR /deployments

# Copy the built application from the build stage
COPY --from=build /app/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build /app/target/quarkus-app/*.jar /deployments/
COPY --from=build /app/target/quarkus-app/app/ /deployments/app/
COPY --from=build /app/target/quarkus-app/quarkus/ /deployments/quarkus/

# Set environment variables
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

# Expose the application port
EXPOSE 2020

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar $JAVA_APP_JAR"]
