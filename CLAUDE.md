# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot modular monolith e-commerce application demonstrating Spring Modulith features. The application is built around a bookstore domain with clearly separated business modules that communicate through events and well-defined APIs.

## Development Commands

### Build and Test
```bash
# Run tests (includes formatting check)
task test

# Or using Maven directly
./mvnw clean verify

# Format code using Spotless
task format
# or
./mvnw spotless:apply

# Build without tests
./mvnw clean compile
```

### Running the Application

#### Local Development (with Docker services)
```bash
# Build Docker image and start all services
task start

# Stop services
task stop

# Restart services
task restart

# Run just the application (requires external PostgreSQL/RabbitMQ)
./mvnw spring-boot:run
```

#### Kubernetes Development
```bash
# Create KinD cluster
task kind_create

# Deploy to cluster
task k8s_deploy

# Clean up
task k8s_undeploy
task kind_destroy
```

### Testing

#### Run All Tests
```bash
task test
```

#### Run Specific Test Classes
```bash
# Module-specific tests
./mvnw test -Dtest=CatalogIntegrationTests
./mvnw test -Dtest=OrdersIntegrationTests
./mvnw test -Dtest=InventoryIntegrationTests

# Modularity verification
./mvnw test -Dtest=ModularityTests

# REST API tests
./mvnw test -Dtest=ProductRestControllerTests
./mvnw test -Dtest=OrderRestControllerTests
```

#### Integration Tests
```bash
# Run all integration tests
./mvnw verify -Dit.test="**/*IntegrationTests"
```

## Architecture Overview

### Module Structure
The application follows Spring Modulith principles with these business modules:

- **Common**: Shared code and utilities (open module)
- **Catalog**: Product catalog management (stores in `catalog` schema)
- **Orders**: Order management (stores in `orders` schema)  
- **Inventory**: Stock management (stores in `inventory` schema)
- **Notifications**: Event-driven notifications

### Module Communication Patterns

#### API-based Communication
- Orders module calls Catalog module's public API (`ProductApi`) for product validation
- Each module exposes a public API component (e.g., `ProductApi`, `OrderApi`)

#### Event-driven Communication
- Orders publishes `OrderCreatedEvent` when order is successfully created
- Events are published to both internal Spring Modulith event bus and external RabbitMQ
- Inventory module consumes `OrderCreatedEvent` to update stock levels
- Notifications module consumes `OrderCreatedEvent` to send confirmation emails

#### Data Isolation
- Each module manages its own database schema
- No direct cross-module database access
- PostgreSQL with separate schemas per module

### Key Spring Modulith Features Used

#### Module Verification
```java
// In ModularityTests.java
ApplicationModules.of(BookStoreApplication.class).verify();
```

#### Event Publishing
```java
@Component
class OrderService {
    @EventListener
    void publishOrderCreated(OrderCreatedEvent event) {
        // Event automatically published to other modules
    }
}
```

#### Module Testing
```java
@ApplicationModuleTest
class CatalogIntegrationTests {
    // Test only loads catalog module components
}
```

## Technology Stack

### Core Framework
- **Spring Boot 3.5.5** with Java 21
- **Spring Modulith 1.4.3** for modular architecture
- **Spring Data JPA** for persistence
- **Spring AMQP** for messaging

### Database & Migration
- **PostgreSQL** as primary database
- **Flyway** for database migrations
- **Separate schemas** per module for data isolation

### Messaging & Events
- **RabbitMQ** for external event publishing
- **Spring Modulith Events** for internal module communication
- **JDBC-based event store** with automatic republishing

### Observability
- **Micrometer** with Prometheus registry
- **OpenTelemetry** tracing with Zipkin export
- **Spring Actuator** with modulith endpoints

### Frontend
- **Thymeleaf** templating
- **HTMX** for dynamic interactions  
- **Bootstrap** for UI styling

### Code Quality
- **Spotless** with Palantir Java Format for code formatting
- **Enhanced test reporting** with JUnit 5 tree reporter

## Development Guidelines

### Module Design Principles
1. **Independence**: Each module should be as self-contained as possible
2. **Event-first**: Prefer event-driven communication over direct API calls
3. **Data Ownership**: Each module owns its data and schema
4. **Public APIs**: Expose functionality through dedicated API components
5. **Testability**: Modules should be testable in isolation using `@ApplicationModuleTest`

### Adding New Modules
1. Create package under `com.sivalabs.bookstore.[modulename]`
2. Add `package-info.java` to define module boundaries
3. Create dedicated database schema and Flyway migrations
4. Implement public API component for cross-module access
5. Add module-specific integration test with `@ApplicationModuleTest`
6. Update `ModularityTests` to verify new module structure

### Event Handling Best Practices
- Use `@EventListener` for consuming events within the same module
- Events are automatically published externally via RabbitMQ configuration
- Events are persisted and can be replayed on application restart
- Design events as immutable data structures

### Database Schema Management
- Each module manages its own Flyway migrations in `db/migration/[module]/`
- Use module-specific schema names (e.g., `catalog`, `orders`, `inventory`)
- No cross-schema foreign keys or joins

## Configuration

### Application URLs
- **Application**: http://localhost:8080
- **Actuator**: http://localhost:8080/actuator
- **Modulith Info**: http://localhost:8080/actuator/modulith
- **RabbitMQ Admin**: http://localhost:15672 (guest/guest)
- **Zipkin**: http://localhost:9411

### Key Configuration Properties
- **Database**: PostgreSQL connection via `spring.datasource.*`
- **Events**: JDBC event store with schema initialization
- **Tracing**: Full sampling with Zipkin export
- **RabbitMQ**: Local connection for event publishing

### Environment Setup
- **Java 21** (recommended: install via SDKMAN)
- **Docker & Docker Compose** for running dependencies
- **Task runner** (go-task) for simplified command execution
- **Maven Wrapper** included in project