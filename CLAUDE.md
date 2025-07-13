# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**peah.db** is a Quarkus-based microservice for database management APIs using:
- Java 21 with Quarkus 3.24.3
- PostgreSQL with Hibernate ORM and Flyway migrations
- MapStruct 1.6.3 and Lombok 1.18.30 for code generation
- REST APIs with Jackson, OpenAPI documentation
- Containerization via Jib, Kubernetes deployment via Helm

## Essential Commands

### Development
```bash
# Run in dev mode with hot reload
./mvnw quarkus:dev

# Run with database connection
DB_USERNAME=postgres DB_PASSWORD=postgres DB_URL=jdbc:postgresql://localhost:5432/peahdb ./mvnw quarkus:dev

# Run with debugging on port 5005
./mvnw quarkus:dev -Ddebug=5005
```

### Testing
```bash
# Run unit tests
./mvnw test

# Run specific test
./mvnw test -Dtest=GreetingResourceTest

# Run all tests including integration tests
./mvnw verify -DskipITs=false

# Run tests for native build
./mvnw verify -Dnative
```

### Building
```bash
# Build JVM package
./mvnw clean package

# Build native executable
./mvnw clean package -Dnative

# Build container image
./mvnw clean package -Dquarkus.container-image.build=true

# Build and push container
./mvnw clean package -Dquarkus.container-image.build=true -Dquarkus.container-image.push=true
```

### Development Endpoints
- Application: http://localhost:8080
- Dev UI: http://localhost:8080/q/dev/
- Swagger/OpenAPI: http://localhost:8080/api-docs
- Health: http://localhost:8080/q/health
- Metrics: http://localhost:8080/q/metrics

## Architecture & Code Structure

### Package Structure
```
com.lolmeida/
├── resources/          # REST endpoints
├── services/          # Business logic
├── entities/          # JPA entities
├── dto/              # Data transfer objects
├── mappers/          # MapStruct mappers
└── health/           # Health checks
```

### Key Architectural Patterns

1. **REST Resources**: Use `@Path`, `@GET`, `@POST`, etc. annotations. Resources should be in the `resources` package and follow Quarkus REST patterns.

2. **Dependency Injection**: Use CDI annotations (`@ApplicationScoped`, `@RequestScoped`, `@Inject`). Prefer constructor injection.

3. **Database Entities**: JPA entities go in `entities` package. Use Lombok for boilerplate reduction.

4. **DTOs and Mapping**: Use MapStruct for entity-DTO conversion. Define mappers as interfaces with `@Mapper(componentModel = "cdi")`.

5. **Configuration**: Use `@ConfigProperty` for injecting configuration. Database config uses environment variables (DB_USERNAME, DB_PASSWORD, DB_URL).

6. **Database Migrations**: Place Flyway migrations in `src/main/resources/db/migration/` following naming convention `V{version}__{description}.sql`.

### Development Workflow

1. **Adding New Endpoints**:
   - Create resource class in `com.lolmeida.resources`
   - Use appropriate JAX-RS annotations
   - Inject services via CDI
   - Document with OpenAPI annotations

2. **Database Changes**:
   - Create migration file in `db/migration/`
   - Update corresponding entity
   - Update DTOs and mappers if needed
   - Flyway runs migrations automatically on startup

3. **Adding Dependencies**:
   - Check if Quarkus extension exists first
   - Add to `pom.xml` within appropriate dependency management
   - Prefer Quarkus-specific versions when available

4. **Container Deployment**:
   - Images built via Jib (configured in application.properties)
   - Helm chart in `/k8s/` for Kubernetes deployment
   - Update image references in Helm values when deploying

### Important Configuration

**application.properties** key settings:
- Database connection via environment variables
- Flyway migrations enabled at startup
- SQL logging enabled for development
- CORS enabled (restrict in production)
- Container image uses Jib builder
- OpenAPI/Swagger auto-generated from annotations

### Testing Strategy

- Unit tests: Test individual components with mocks
- Integration tests: Use `@QuarkusIntegrationTest` for native tests
- REST tests: Use REST Assured for API testing
- Test database: Configure separate test profile in `application-test.properties`

## Common Tasks

### Local Database Setup
```bash
# PostgreSQL via Docker
docker run -d \
  --name peahdb-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=peahdb \
  -p 5432:5432 \
  postgres:15
```

### Debugging
- Dev mode includes debugging by default
- Access Dev UI for configuration, beans, and routes inspection
- SQL queries logged to console when `quarkus.hibernate-orm.log.sql=true`

### Performance Monitoring
- Micrometer metrics available at `/q/metrics`
- Health checks at `/q/health/live` and `/q/health/ready`
- Custom metrics can be added via Micrometer API