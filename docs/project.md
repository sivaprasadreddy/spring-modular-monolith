# Project: BookStore Modular Monolith

## Mission
A reference e-commerce application (online bookstore) built to demonstrate Spring Modulith features in a practical setting. 
It serves developers learning how to apply modular monolith architecture with Spring Boot — showing event-driven inter-module communication, 
isolated schemas per module, and module-scoped testing.

## Tech Stack
- Language: Java 25
- Framework: Spring Boot 4.x + Spring Modulith 2.x
- Build tool: Maven
- Database: PostgreSQL
- ORM: Spring Data JPA (Hibernate)
- Migrations: Flyway (per-module subdirectories under `db/migration/<module>/`)
- Messaging: RabbitMQ (Spring AMQP + Spring Modulith event externalization)
- Testing: JUnit, Testcontainers (PostgreSQL, RabbitMQ, Grafana), Spring Modulith Test (`@ApplicationModuleTest`)
- Other: Spring Security, Thymeleaf + HTMX + Bootstrap 5 (web UI), OpenTelemetry / Grafana LGTM observability, NullAway + ErrorProne, Spotless (Palantir Java Format), Taskfile

## Architecture
Modular Monolith using Spring Modulith. Each top-level package under `com.sivalabs.bookstore` is a module. 
Modules communicate via Spring application events (internally) and RabbitMQ (externally via Spring Modulith event externalization).

Modules:
- **common** — shared types (e.g., `PagedResult`); open module, usable by all
- **config** — cross-cutting infrastructure config (security, RabbitMQ, etc.)
- **catalog** — product catalogue; public API via `ProductApi` interface
- **orders** — order management; depends on `catalog` and `users`; publishes `OrderCreatedEvent`
- **inventory** — stock management; consumes `OrderCreatedEvent`
- **notifications** — email notifications; consumes `OrderCreatedEvent`
- **users** — user accounts, registration

Package structure per module:
```
com.sivalabs.bookstore.<module>             ← public API types (DTOs, interfaces)
com.sivalabs.bookstore.<module>.domain      ← entities, repositories, services (internal)
com.sivalabs.bookstore.<module>.web         ← Spring MVC controllers (internal)
```
