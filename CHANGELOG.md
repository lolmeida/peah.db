# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2025-07-18

### Added
- **Kubernetes & Helm deployment** with complete chart configuration
- **Production environment** deployed at https://peah-db.lolmeida.com
- **Secret management** with configurable database credentials via Helm values
- **Database environment variables** (DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD)
- **Configurable Quarkus profile** via QUARKUS_PROFILE environment variable
- **Swagger UI in production** - enabled at https://peah-db.lolmeida.com/api-docs/
- **Ingress configuration** with SSL/TLS certificates via Let's Encrypt
- **Health check endpoints** for Kubernetes liveness and readiness probes
- **Automated deployment script** (deploy.sh) with comprehensive logging

### Fixed
- **Database connection issues** in production environment
- **Secret template** now uses dynamic naming (peah-db-k8s-secret)
- **PostgreSQL configuration** with correct connection parameters
- **Image pull policy** set to "Always" for latest image updates
- **Datasource configuration** properly scoped to prod profile
- **Environment variable resolution** for database connection strings

### Changed
- **Database configuration** moved from hardcoded values to environment variables
- **Production profile** now uses PostgreSQL instead of MySQL
- **Deployment strategy** changed from manual to Helm-based
- **Documentation** updated with complete deployment and troubleshooting guide

### Infrastructure
- **Namespace**: lolmeida
- **Deployment**: peah-db-k8s
- **Service**: ClusterIP with port 8080
- **Ingress**: nginx with HTTPS redirect
- **Database**: PostgreSQL (n8n-postgres:5432/n8n)
- **SSL**: Automatic certificate management

## [Unreleased]

### Added
- Comprehensive test suite for MonitoringResource with 10 tests covering:
  - Health check endpoint with request info
  - Request info extraction and response
  - Request summary endpoint
  - Error handling scenarios
  - Mock interactions with RequestInfoExtractor
- Comprehensive test suite for UserResource with 15 tests covering:
  - GET /users - List all users (with pagination and filters)
  - GET /users/{id} - Get user by ID (success and not found)
  - POST /users - Create user (success and conflict)
  - PUT /users/{id} - Replace user (success, not found, and conflict)
  - PATCH /users/{id} - Partial update (success and not found)
  - DELETE /users/{id} - Delete user (success and not found)
  - GET /api/users - List users with metadata
  - GET /api/users/{id} - Get user with metadata
- Integration tests for UserRepository with 29 tests using Testcontainers
- Total test coverage increased to 141 tests across all components

### Fixed
- Fixed resource layer tests by properly mocking service dependencies
- Resolved issues with @InjectMock annotations in QuarkusTest
- Fixed test initialization and proper response validation

### Changed
- Updated README.md to reflect current test coverage (141 total tests)
- Improved test documentation with detailed breakdown by component
- Enhanced test metrics section with comprehensive coverage information

## [1.2.0] - 2025-07-18

### Added
- Comprehensive OpenAPI documentation for all REST endpoints
- OpenAPIConfig with complete API specification
- Detailed schema documentation for all DTOs
- API examples for all request/response bodies
- Server configurations for dev, staging, and production environments

### Fixed
- Test suite improvements for better if/else clause coverage
- Enhanced test DisplayName annotations for clarity
- Removed Portuguese comments from test files

## [1.1.0] - 2025-07-17

### Added
- AuditService with 34 comprehensive tests
- RequestLogService with 19 comprehensive tests
- Enhanced monitoring and logging capabilities
- Device detection and browser identification
- Custom HTTP headers for request tracking

### Changed
- Improved test coverage for conditional logic
- Enhanced error handling in services
- Updated MapStruct mappers for better null handling

## [1.0.0] - 2025-07-16

### Added
- Initial release of peah.db API
- User management REST API with full CRUD operations
- MySQL/PostgreSQL support with Hibernate ORM
- Flyway database migrations
- MapStruct for DTO mapping
- Comprehensive UserService with 28 unit tests
- Kubernetes deployment with Helm charts
- GitHub Actions CI/CD pipeline
- Monitoring endpoints with request tracking
- Health checks and metrics endpoints
- Development and production profiles
- Docker containerization with Jib