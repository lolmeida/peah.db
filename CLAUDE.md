
# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**peah.db** is a Quarkus-based microservice for database management APIs using:
- Java 21 with Quarkus 3.24.3
- MySQL (development) / PostgreSQL (production) with Hibernate ORM
- Flyway for database migrations
- MapStruct 1.6.3 and Lombok 1.18.30 for code generation
- REST APIs with Jackson, OpenAPI documentation
- Containerization via Jib, Kubernetes deployment via Helm
- GitHub Actions for CI/CD

## Essential Commands

### Development
```bash
# Run in dev mode with hot reload (auto-starts MySQL via Dev Services)
./mvnw quarkus:dev

# Run with custom database connection
DB_USERNAME=root DB_PASSWORD=root DB_URL=jdbc:mysql://localhost:3306/peahdb ./mvnw quarkus:dev

# Run with debugging on port 5005
./mvnw quarkus:dev -Ddebug=5005

# Run with specific profile
./mvnw quarkus:dev -Dquarkus.profile=prod
```

### Testing
```bash
# Run unit tests
./mvnw test

# Run specific test
./mvnw test -Dtest=UserResourceTest

# Run all tests including integration tests
./mvnw verify -DskipITs=false

# Run tests for native build
./mvnw verify -Dnative
```

### Building
```bash
# Build JVM package
./mvnw clean package

# Build über-jar (single executable jar)
./mvnw package -Dquarkus.package.jar.type=uber-jar

# Build native executable
./mvnw clean package -Dnative

# Build native with container (no local GraalVM needed)
./mvnw package -Dnative -Dquarkus.native.container-build=true

# Build container image
./mvnw clean package -Dquarkus.container-image.build=true

# Build and push container
./mvnw clean package -Dquarkus.container-image.build=true -Dquarkus.container-image.push=true
```

### Deployment
```bash
# Deploy to Kubernetes (uses MicroK8s)
./deploy.sh [image-tag]

# Manual Helm deployment
helm upgrade --install peah-db k8s/ --namespace lolmeida --create-namespace
```

### Development Endpoints
- Application: http://localhost:8080
- Dev UI: http://localhost:8080/q/dev/
- Swagger/OpenAPI: http://localhost:8080/api-docs (dev only)
- Health: http://localhost:8080/q/health
- Metrics: http://localhost:8080/q/metrics

## Architecture & Code Structure

### Package Structure
```
com.lolmeida.peahdb/
├── resource/          # REST endpoints
├── service/           # Business logic
├── entity/            # JPA entities
├── dto/               # Data transfer objects
│   ├── request/       # Request DTOs
│   ├── response/      # Response DTOs
│   └── mapper/        # MapStruct mappers
├── repository/        # Data access layer
├── health/            # Health checks
└── config/            # Configuration classes
```

### Key Architectural Patterns

1. **REST Resources**: Use `@Path`, `@GET`, `@POST`, etc. annotations. Resources should be in the `resource` package and follow Quarkus REST patterns.

2. **Dependency Injection**: Use CDI annotations (`@ApplicationScoped`, `@RequestScoped`, `@Inject`). Prefer constructor injection.

3. **Database Entities**: JPA entities go in `entity` package. Use Lombok for boilerplate reduction. Entities can extend PanacheEntity for simplified data access.

4. **DTOs and Mapping**: Use MapStruct for entity-DTO conversion. Define mappers as interfaces with `@Mapper(componentModel = "cdi")` in the `dto.mapper` package.

5. **Configuration**: Use `@ConfigProperty` for injecting configuration. Database config differs by profile:
   - Dev: MySQL with Dev Services or environment variables
   - Prod: PostgreSQL with `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`

6. **Database Migrations**: Place Flyway migrations in `src/main/resources/db/migration/` following naming convention `V{version}__{description}.sql`.

### Development Workflow

1. **Adding New Endpoints**:
   - Create resource class in `com.lolmeida.peahdb.resource`
   - Use appropriate JAX-RS annotations
   - Inject services via CDI
   - Document with OpenAPI annotations

2. **Database Changes**:
   - Create migration file in `db/migration/`
   - Update corresponding entity
   - Update DTOs and mappers if needed
   - Dev profile: migrations run automatically with drop-and-create
   - Prod profile: migrations disabled at startup (manual control)

3. **Adding Dependencies**:
   - Check if Quarkus extension exists first
   - Add to `pom.xml` within appropriate dependency management
   - Prefer Quarkus-specific versions when available

4. **Container Deployment**:
   - Images built via Jib to `docker.io/lolmeida/peah-db`
   - Configure via environment variables: `CONTAINER_IMAGE_*`
   - Helm chart in `/k8s/` for Kubernetes deployment
   - Expects `peah-db-postgres-secret` in namespace

### Important Configuration

**Profile-Specific Settings**:
- **Development** (`application-dev.properties`):
  - MySQL with Quarkus Dev Services
  - Hibernate drop-and-create mode
  - CORS enabled for localhost:3000/8080
  - Flyway baseline on migrate
  
- **Production** (`application-prod.properties`):
  - PostgreSQL with environment variables
  - Flyway migrations disabled at startup
  - File logging to `logs/application.log`
  - Swagger UI disabled

**Environment Variables**:
- `APPLICATION_NAME`: Override application name
- Database: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- Container: `CONTAINER_IMAGE_BUILD`, `CONTAINER_IMAGE_PUSH`, etc.

### Testing Strategy

- Unit tests: Test individual components with mocks
- Integration tests: Use `@QuarkusIntegrationTest` with Testcontainers for MySQL
- REST tests: Use REST Assured for API testing
- Native tests: Run with `-Dnative` flag

## Common Tasks

### Local Database Setup
```bash
# MySQL via Docker (for manual testing)
docker run -d \
  --name peahdb-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=peahdb \
  -p 3306:3306 \
  mysql:8

# PostgreSQL via Docker (for production-like testing)
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
- Prometheus metrics endpoint enabled
- OpenTelemetry bridge configured

### CI/CD Workflow
- GitHub Actions automatically builds and pushes Docker images on main branch
- Manual deployment workflow available for VPS deployment
- Deployment target: MicroK8s cluster on VPS (31.97.53.64)