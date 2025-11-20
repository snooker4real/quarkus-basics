# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Quarkus 3.29.4 application using **Java 25** that connects to a MySQL database (Sakila schema). The project demonstrates modern Java features including records, pattern matching, sealed interfaces, and sequenced collections. It includes Hibernate ORM with Panache for database access, REST endpoints with Jackson serialization, JPAStreamer for stream-based queries, and OpenAPI/Swagger UI documentation.

### Java 25 Features Used
- **Records**: DTOs (FilmDTO, ActorDTO) for immutable data carriers
- **Pattern Matching**: Type-safe conditional logic throughout
- **Switch Expressions**: Modern, expression-based switches
- **Sealed Interfaces**: Type-safe result handling (ValidationResult, RentalRateUpdateResult)
- **Sequenced Collections**: First/last element access in collections
- **Text Blocks**: Multi-line string literals for formatted output
- **Enhanced instanceof**: Pattern matching with type extraction

See `JAVA25_FEATURES.md` for comprehensive documentation of Java 25 features.

## Essential Commands

### Development
```bash
./mvnw quarkus:dev
```
Starts the application in dev mode with live reload. The Dev UI is accessible at http://localhost:8080/q/dev/

### Testing
```bash
./mvnw test
```
Run unit tests.

```bash
./mvnw verify
```
Run integration tests (currently skipped by default via `skipITs=true`).

### Building

**Standard package:**
```bash
./mvnw package
```
Produces `target/quarkus-app/quarkus-run.jar` (not an uber-jar). Run with:
```bash
java -jar target/quarkus-app/quarkus-run.jar
```

**Uber-jar:**
```bash
./mvnw package -Dquarkus.package.jar.type=uber-jar
```
Run with: `java -jar target/*-runner.jar`

**Native executable (requires GraalVM):**
```bash
./mvnw package -Dnative
```

**Native executable (containerized build):**
```bash
./mvnw package -Dnative -Dquarkus.native.container-build=true
```
Run with: `./target/quarkus-basics-1.0.0-SNAPSHOT-runner`

### Docker

Four Dockerfile variants are available in `src/main/docker/`:

1. **JVM mode:**
```bash
./mvnw package
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/quarkus-basics-jvm .
docker run -i --rm -p 8080:8080 quarkus/quarkus-basics-jvm
```

2. **Legacy JAR:** `Dockerfile.legacy-jar`
3. **Native:** `Dockerfile.native`
4. **Native micro:** `Dockerfile.native-micro`

## Architecture

### Database Configuration

**Connection details** in `src/main/resources/application.properties`:
- Database: MySQL at `localhost:3309`
- Schema: `sakila`
- Username/password configured (currently hardcoded - should be externalized for production)
- SQL logging enabled via `quarkus.hibernate-orm.log.sql=true`

**Important:** There's a typo in the current configuration: `quarkus.datasource.jubc.url` should be `quarkus.datasource.jdbc.url`

### Domain Model

The application uses JPA entities mapped to the Sakila database schema:

**Actor** (`com.snooker4real.model/Actor.java`):
- Maps to `sakila.actor` table
- Fields: `actorId`, `firstName`, `lastName`, `lastUpdate`
- Many-to-many relationship with Film (bidirectional, mapped by `films`)

**Film** (`com.snooker4real.model/Film.java`):
- Maps to `sakila.film` table
- Fields include: `filmId`, `title`, `description`, `releaseYear`, `languageId`, `rentalDuration`, `rentalRate`, `length`, `replacementCost`, `rating`, `specialFeatures`, `lastUpdate`
- Owns the many-to-many relationship with Actor via `film_actor` join table
- Uses `CascadeType.ALL` on the actors relationship

**Relationship structure:**
- Film → Actor: `@ManyToMany` with `@JoinTable(name = "film_actor")`
- Actor → Film: `@ManyToMany(mappedBy = "actors")`

### Technology Stack

- **Quarkus 3.29.4** - Framework
- **Hibernate ORM with Panache** - Simplified persistence
- **JPAStreamer 3.0.3.Final** - Stream-based database queries
- **Quarkus REST with Jackson** - REST endpoints and JSON serialization
- **SmallRye OpenAPI** - API documentation with Swagger UI
- **MySQL JDBC Driver** - Database connectivity
- **Maven** - Build tool

### Package Structure

```
src/main/java/
  └── com.snooker4real.model/           # JPA entity classes (Actor, Film)
src/main/resources/
  └── application.properties
src/main/docker/       # Docker build files
target/
  └── generated-sources/annotations/  # JPAStreamer metamodel classes (Actor$, Film$)
```

## Key Implementation Details

1. **JPA Entities**: All entities use standard JPA annotations with Jakarta Persistence API 3.2.0
2. **Generated Metamodel**: JPAStreamer generates metamodel classes (e.g., `Actor$`, `Film$`) in `target/generated-sources/annotations/` for type-safe stream queries
3. **Bidirectional Relationships**: The Film-Actor many-to-many relationship is bidirectional; modifications should be made on the Film side (owning side)
4. **MySQL Enums**: Film entity uses MySQL enum types for `rating` and `specialFeatures` fields (currently mapped as Object type)
5. **Test Configuration**: Surefire plugin configured with `--add-opens java.base/java.lang=ALL-UNNAMED` for Java 17 compatibility

## Common Pitfalls

- The datasource URL property has a typo (`jubc` instead of `jdbc`)
- Database credentials are hardcoded in `application.properties` - use environment variables or profiles for production
- The `rating` and `specialFeatures` fields in Film are typed as `Object` - consider creating Java enums for type safety
- No REST resources are currently implemented - only domain models exist
- No tests are present in the repository yet
