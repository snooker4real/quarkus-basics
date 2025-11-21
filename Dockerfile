####
# This Dockerfile is used to build a Quarkus application for production deployment on Dokploy
# Requires Java 25 for modern Java features
####

## Stage 1: Build the application
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# First, compile to generate JPAStreamer metamodel classes (Film$, Actor$, etc.)
RUN mvn compile -B

# Then package the application
RUN mvn package -DskipTests -B

## Stage 2: Create the runtime image
FROM eclipse-temurin:25-jre

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
