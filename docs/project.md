# Project: BookStore Modular Monolith

## Mission
A reference e-commerce application (online bookstore) built to demonstrate Spring Modulith features in a practical setting. 
It serves developers learning how to apply modular monolith architecture with Spring Boot — showing event-driven inter-module communication, 
isolated schemas per module, and module-scoped testing.

## Tech Stack
- Language: Java 25
- Framework: Spring Boot 4.0.6 + Spring Modulith 2.0.6
- Build tool: Maven
- Database: PostgreSQL 18
- ORM: Spring Data JPA (Hibernate)
- Migrations: Flyway (per-module subdirectories under `db/migration/<module>/`)
- Messaging: RabbitMQ 4.x (Spring AMQP + Spring Modulith event externalization)
- Testing: JUnit 5, Testcontainers (PostgreSQL, RabbitMQ, Grafana), Spring Modulith Test (`@ApplicationModuleTest`)
- Other: Spring Security (form login + OAuth2 JWT resource server), Thymeleaf + HTMX + Bootstrap 5 (web UI), SpringDoc OpenAPI 3, OpenTelemetry / Grafana LGTM observability, NullAway + ErrorProne, Spotless (Palantir Java Format), Taskfile

## Architecture
Modular Monolith using Spring Modulith. Each top-level package under `com.sivalabs.bookstore` is a module, declared with `@ApplicationModule` in `package-info.java`. Modules communicate via Spring application events (internally) and RabbitMQ (externally via Spring Modulith event externalization).

Modules:
- **common** — shared types (e.g., `PagedResult`); open module, usable by all
- **catalog** — product catalogue; public API via `ProductApi` interface
- **orders** — order management; depends on `catalog` and `users`; publishes `OrderCreatedEvent`
- **inventory** — stock management; consumes `OrderCreatedEvent`
- **notifications** — email notifications; consumes `OrderCreatedEvent`
- **users** — user accounts, JWT auth, registration
- **config** — cross-cutting infrastructure config (security, RabbitMQ, OpenAPI, etc.)

Package structure per module:
```
com.sivalabs.bookstore.<module>              ← public API types (DTOs, interfaces)
com.sivalabs.bookstore.<module>.domain      ← entities, repositories, services (internal)
com.sivalabs.bookstore.<module>.web         ← Spring MVC controllers (internal)
```

## Conventions
- Package naming: `com.sivalabs.bookstore.<module>.<layer>` — internal sub-packages are package-private by convention
- REST base path: `/api/` — no version segment (e.g., `/api/products`, `/api/orders`)
- Error handling: `@RestControllerAdvice` per module (e.g., `OrdersExceptionHandler`) extending `ResponseEntityExceptionHandler`; error responses use RFC 7807 `ProblemDetail`
- Authentication (REST API, `@Order(1)`): JWT Bearer token via OAuth2 Resource Server; stateless sessions; public endpoints are `/api/login`, `POST /api/users`, `GET /api/products/**`
- Authentication (Web UI, `@Order(2)`): form login at `/login`; public paths include `/products`, `/buy`, `/cart`; admin paths require `ROLE_ADMIN`
- Null safety: all packages annotated `@NullMarked` (jspecify); NullAway enforced at compile time with `ERROR` severity
- Code style: Spotless with Palantir Java Format — run `task format` to auto-format before committing
- Flyway migrations: one subdirectory per module (`db/migration/<module>/`), plus `__root/` for shared schema (events table)
- Module events: published via `ApplicationEventPublisher`, persisted in the `events` schema by Spring Modulith JDBC event store, then forwarded to RabbitMQ for external consumers
- Module tests: use `@ApplicationModuleTest` to load only the module under test in isolation

## Approved Dependencies
- Spring Boot 4.0.6 starters (web MVC, data JPA, validation, security, actuator, thymeleaf, AMQP, devtools, docker-compose)
- Spring Modulith 2.0.6 (core, JDBC event store, AMQP externalization, actuator, observability, test)
- Spring Security OAuth2 Resource Server (JWT)
- PostgreSQL JDBC driver
- Flyway + `flyway-database-postgresql`
- RabbitMQ / Spring AMQP
- SpringDoc OpenAPI 3 (`springdoc-openapi-starter-webmvc-ui` 3.0.3)
- Thymeleaf + Thymeleaf Layout Dialect + Thymeleaf Spring Security extras
- HTMX Spring Boot Thymeleaf (`htmx-spring-boot-thymeleaf` 5.1.0)
- Bootstrap 5 + HTMX 2 (via WebJars)
- OpenTelemetry / Micrometer / `datasource-micrometer-spring-boot`
- Testcontainers (JUnit Jupiter, PostgreSQL, RabbitMQ, Grafana)
- NullAway 0.13.4 + ErrorProne 2.49.0
- Spotless 3.5.1 + Palantir Java Format 2.90.0
- JSpecify (via `@NullMarked`)
- Anything outside this list should be flagged before adding.
