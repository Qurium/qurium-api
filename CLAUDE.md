# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Dev mode with live reload (starts Dev UI at http://localhost:8080/q/dev/)
./mvnw quarkus:dev

# Run unit tests
./mvnw test

# Run a single test
./mvnw test -Dtest=ExampleResourceTest

# Run integration tests (requires running app or native binary)
./mvnw verify -DskipITs=false

# Build JAR
./mvnw package

# Build native executable (requires GraalVM)
./mvnw package -Dnative

# Build native in container (no GraalVM needed)
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

## Local Database

Start PostgreSQL via Docker Compose before running the app locally:

```bash
docker compose up db
```

Default connection: `jdbc:postgresql://localhost:5432/qurium` (user: `qurium`, password: `change-me`).

Override via env vars: `QURIUM_DATASOURCE_URL`, `QURIUM_DATASOURCE_USERNAME`, `QURIUM_DATASOURCE_PASSWORD`, `QURIUM_DATASOURCE_DB`.

## Architecture

Quarkus 3.x REST API (Java 25) backed by PostgreSQL.

**Key stack choices:**
- **REST layer**: `quarkus-rest` + `quarkus-rest-jackson` (Jakarta REST, not RESTEasy — these are incompatible, don't mix)
- **Persistence**: Hibernate ORM with Panache (`quarkus-hibernate-orm-panache`) using the **active record pattern** — entities extend `PanacheEntity` and call persistence methods directly (e.g., `entity.persist()`, `MyEntity.listAll()`)
- **Database drivers**: Both JDBC (`quarkus-jdbc-postgresql`) and reactive (`quarkus-reactive-pg-client`) are present
- **Validation**: `quarkus-hibernate-validator`
- **DI**: Quarkus ArC (CDI-based)

**Test approach**: `@QuarkusTest` with REST Assured for endpoint testing. Tests in `src/test/` use `import.sql` for seed data (currently commented out — uncomment to seed test DB).

**Docker**: `docker-compose.yml` includes both `db` and `api` services. The `api` service builds from `Dockerfile` and connects to `db` via service name.