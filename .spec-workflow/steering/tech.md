# Technology Stack

## Project Type
Spring Boot modular monolith e-commerce web application demonstrating Spring Modulith features with event-driven architecture and clear module boundaries.

## Core Technologies

### Primary Language(s)
- **Language**: Java 21 (LTS version with modern language features)
- **Runtime/Compiler**: OpenJDK 21 (recommended via SDKMAN)
- **Language-specific tools**: Maven 3.9.x (via Maven Wrapper), Spotless with Palantir Java Format

### Key Dependencies/Libraries
- **Spring Boot 3.5.5**: Core framework with auto-configuration and production-ready features
- **Spring Modulith 1.4.3**: Modular architecture support with event-driven communication
- **Spring Data JPA**: Database abstraction layer with repository pattern
- **Spring AMQP**: RabbitMQ integration for external event publishing
- **PostgreSQL Driver**: Database connectivity for multi-schema persistence
- **Flyway**: Database migration and versioning management
- **Thymeleaf**: Server-side templating engine for web views
- **HTMX**: Modern web interactions without complex JavaScript frameworks
- **Bootstrap**: CSS framework for responsive UI design

### Application Architecture
**Spring Modulith-based modular monolith** with the following characteristics:
- **Module-driven design**: Business logic organized in self-contained modules
- **Event-driven communication**: Internal Spring Modulith events + external RabbitMQ
- **API-based integration**: Public APIs for cross-module communication
- **Data ownership**: Each module manages its own database schema
- **Hexagonal architecture**: Clear separation between domain, infrastructure, and web layers

**Module Structure**:
- **Common**: Shared utilities and configurations (open module)
- **Catalog**: Product catalog management
- **Orders**: Order processing and lifecycle
- **Inventory**: Stock management and tracking
- **Notifications**: Event-driven notification system

### Data Storage
- **Primary storage**: PostgreSQL 15+ with schema-per-module approach
- **Event storage**: JDBC-based event store for Spring Modulith events
- **Schema isolation**: `catalog`, `orders`, `inventory` schemas for data boundaries
- **Migration management**: Flyway with module-specific migration paths

### External Integrations
- **Messaging**: RabbitMQ for external event publishing and cross-service communication
- **Protocols**: HTTP/REST APIs, AMQP messaging, JDBC database connections
- **Event patterns**: Domain events, event sourcing capabilities, automatic republishing

### Monitoring & Dashboard Technologies
- **Observability**: Micrometer with Prometheus registry for metrics collection
- **Distributed Tracing**: OpenTelemetry with Zipkin export for request tracing
- **Application Monitoring**: Spring Actuator with custom modulith endpoints
- **Health Checks**: Spring Boot health indicators with module-specific checks

## Development Environment

### Build & Development Tools
- **Build System**: Maven 3.9.x with Maven Wrapper for reproducible builds
- **Task Runner**: Go-Task (task) for simplified command execution and workflow automation
- **Development workflow**: Spring Boot DevTools for hot reload, embedded server support
- **Container Development**: Docker Compose for local services (PostgreSQL, RabbitMQ, Zipkin)

### Code Quality Tools
- **Static Analysis**: Integrated via Maven plugins and IDE support
- **Formatting**: Spotless with Palantir Java Format for consistent code style
- **Testing Framework**: JUnit 5 with Spring Boot Test, TestContainers for integration tests
- **Module Testing**: `@ApplicationModuleTest` for isolated module testing
- **Documentation**: JavaDoc generation, automated API documentation

### Version Control & Collaboration
- **VCS**: Git with conventional commit messages
- **Branching Strategy**: Feature branch workflow with PR-based reviews
- **Code Review Process**: GitHub/GitLab PR reviews with automated CI checks
- **Quality Gates**: Pre-commit hooks for formatting, test execution requirements

### Dashboard Development
- **Live Reload**: Spring Boot DevTools for automatic application restart
- **Port Management**: Configurable ports via Spring profiles (default: 8080)
- **Multi-Instance Support**: Profile-based configuration for different environments

## Deployment & Distribution

### Target Platform(s)
- **Local Development**: Docker Compose with embedded services
- **Kubernetes**: Production deployment with Helm charts and KinD for local testing
- **Cloud Native**: Container-first approach with 12-factor app principles
- **CI/CD**: GitHub Actions or similar for automated testing and deployment

### Distribution Method
- **Container Images**: Docker images built via Maven plugins
- **Helm Charts**: Kubernetes deployment manifests for orchestration
- **JAR Distribution**: Spring Boot fat JARs for traditional deployment
- **Configuration Management**: Externalized configuration via ConfigMaps/environment variables

### Installation Requirements
- **Java Runtime**: OpenJDK 21+ required for execution
- **Database**: PostgreSQL 15+ with module-specific schema creation
- **Message Broker**: RabbitMQ for event-driven communication
- **Container Runtime**: Docker for containerized deployment

### Update Mechanism
- **Rolling Updates**: Kubernetes rolling deployment strategy
- **Database Migrations**: Automatic Flyway migrations on startup
- **Configuration Updates**: ConfigMap updates with pod restarts
- **Feature Flags**: Profile-based feature enablement

## Technical Requirements & Constraints

### Performance Requirements
- **Startup Time**: <30 seconds for full application initialization
- **Response Time**: <200ms for typical web requests, <500ms for complex operations
- **Memory Usage**: <512MB base memory, <1GB under load
- **Throughput**: 100+ concurrent users, 1000+ requests per minute

### Compatibility Requirements  
- **Platform Support**: Linux (production), macOS/Windows (development)
- **Java Version**: OpenJDK 21+ (LTS compatibility)
- **Database Compatibility**: PostgreSQL 15+, H2 for testing
- **Container Standards**: OCI-compliant images, Kubernetes 1.25+

### Security & Compliance
- **Authentication**: Spring Security with configurable providers
- **Data Protection**: Database encryption at rest, TLS in transit
- **Event Security**: Secure message publishing with authentication
- **Dependency Security**: Regular vulnerability scanning via Maven plugins

### Scalability & Reliability
- **Expected Load**: 1000+ products, 10000+ orders, 100+ concurrent users
- **Availability Requirements**: 99.9% uptime target with graceful degradation
- **Growth Projections**: Horizontal scaling via Kubernetes, vertical scaling via resource allocation
- **Event Reliability**: Persistent event store with automatic replay capabilities

## Technical Decisions & Rationale

### Decision Log
1. **Spring Modulith over Microservices**: Chosen for development simplicity while maintaining modularity. Provides clear module boundaries without distributed system complexity. Evolution path to microservices remains available.

2. **PostgreSQL with Schema-per-Module**: Ensures data isolation between modules while maintaining single database simplicity. Supports future database splitting if microservices evolution is needed.

3. **Event-Driven Architecture**: Enables loose coupling between modules and supports future scaling. Spring Modulith events provide internal consistency while RabbitMQ enables external integration.

4. **Thymeleaf + HTMX over SPA Framework**: Reduces complexity for server-side rendered application while providing modern web interactions. Maintains Spring Boot's convention-over-configuration benefits.

5. **Maven over Gradle**: Aligns with Spring Boot ecosystem conventions and provides mature plugin ecosystem. Maven Wrapper ensures build reproducibility across environments.

6. **Docker + Kubernetes Deployment**: Container-first approach provides deployment flexibility and cloud-native capabilities. KinD enables local Kubernetes development.

## Known Limitations

- **Single Database**: While schema isolation exists, true database independence requires future splitting for microservices evolution
- **Monolith Deployment**: All modules deploy together, limiting independent deployment capabilities
- **Event Ordering**: Cross-module event ordering guarantees are limited by Spring Modulith's eventual consistency model
- **Development Complexity**: Module boundaries require discipline to maintain, potential for boundary violations during rapid development
- **Testing Isolation**: Full integration tests require all modules, making test execution slower than pure unit tests