# Demo Product Service

A production-ready sample Java REST API built with **Spring Boot 3 / Java 17**, demonstrating a clean, layered architecture (controller → service → repository), input validation, centralized error handling, database migrations, observability, and containerization.

## Stack / Libraries

| Concern | Library |
|---|---|
| Web framework | Spring Boot Web (Spring MVC) |
| Validation | Spring Boot Validation (Jakarta Bean Validation) |
| Persistence | Spring Data JPA (Hibernate) |
| Database (dev/test) | H2 in-memory |
| Database (prod) | PostgreSQL |
| Schema migrations | Flyway |
| Observability | Spring Boot Actuator + Micrometer/Prometheus |
| API docs | springdoc-openapi (Swagger UI) |
| Boilerplate reduction | Lombok |
| Testing | JUnit 5, Mockito, AssertJ, Spring Boot Test (MockMvc) |
| Build | Maven |
| Containerization | Docker (multi-stage build), docker-compose |

## Project layout

```
src/main/java/com/example/demo
├── DemoApplication.java
├── config/            # OpenAPI + JPA auditing configuration
├── controller/        # REST controllers
├── dto/                # Request/response records
├── entity/             # JPA entities
├── exception/          # Custom exceptions + global @RestControllerAdvice
├── repository/         # Spring Data repositories
└── service/            # Business logic + entity/DTO mapping

src/main/resources
├── application.yml         # shared config
├── application-dev.yml     # H2 dev profile
├── application-prod.yml    # Postgres prod profile
└── db/migration/           # Flyway SQL migrations

src/test/java/...            # unit + integration tests
```

## Running locally (H2, no external dependencies)

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080` with the `dev` profile (H2 in-memory DB) active by default.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health check: `http://localhost:8080/actuator/health`
- H2 console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:demodb`)

## Running tests

```bash
mvn test
```

Includes unit tests (Mockito, service layer) and integration tests (`@SpringBootTest` + MockMvc against an isolated H2 test database, using the `test` profile).

## Building a jar

```bash
mvn clean package
java -jar target/demo.jar
```

## Running in production mode with PostgreSQL (Docker Compose)

```bash
docker compose up --build
```

This starts a PostgreSQL container and the app (built via the multi-stage `Dockerfile`) wired together, with Flyway automatically applying migrations on startup.

Environment variables used in the `prod` profile:

| Variable | Purpose | Default |
|---|---|---|
| `DB_URL` | JDBC URL | `jdbc:postgresql://localhost:5432/demodb` |
| `DB_USERNAME` | DB user | `demo` |
| `DB_PASSWORD` | DB password | `demo` |
| `DB_POOL_SIZE` | HikariCP max pool size | `10` |
| `SERVER_PORT` | HTTP port | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active profile | `dev` |

**Never commit real credentials.** In real deployments, inject `DB_PASSWORD` and similar secrets via your platform's secret manager (Kubernetes Secrets, AWS Secrets Manager, Vault, etc.), not via plain environment variables in source control.

## API

Base path: `/api/v1/products`

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/products?name=&page=&size=&sort=` | List products (paginated, optional name filter) |
| GET | `/api/v1/products/{id}` | Get a single product |
| POST | `/api/v1/products` | Create a product |
| PUT | `/api/v1/products/{id}` | Update a product |
| DELETE | `/api/v1/products/{id}` | Delete a product |

Example create request:

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Widget","description":"A useful widget","price":9.99,"quantity":100}'
```

## Production-readiness notes / what's included

- **Layered architecture** with clear separation of concerns and DTOs (never expose entities directly).
- **Bean validation** on all inputs, with a global exception handler returning a consistent JSON error shape.
- **Optimistic locking** (`@Version`) to guard against lost updates under concurrent writes.
- **Database migrations** via Flyway instead of `ddl-auto: update`, so schema changes are explicit and reviewable.
- **Actuator health/readiness/liveness probes** and **Prometheus metrics**, ready to wire into Kubernetes probes and a metrics scraper.
- **Separate Spring profiles** for dev (H2), test (isolated H2), and prod (PostgreSQL via env vars) — no hardcoded environment-specific config.
- **Multi-stage Docker build** producing a slim runtime image, running as a non-root user, with a container health check.
- **Automated tests**: unit tests for business logic and integration tests exercising the full HTTP stack.

## What you'd still want to add before shipping this to a real production environment

- AuthN/AuthZ (e.g. Spring Security + OAuth2/JWT) — this sample has no authentication.
- Rate limiting / API gateway concerns if exposed publicly.
- Centralized structured logging (JSON logs) and distributed tracing (e.g. OpenTelemetry).
- CI/CD pipeline (build, test, scan, publish image, deploy).
- Dependency vulnerability scanning (e.g. OWASP Dependency-Check, Snyk).
- Environment-specific resource limits/requests if deploying to Kubernetes, plus a Helm chart or manifests.
