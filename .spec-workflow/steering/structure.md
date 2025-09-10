# Project Structure

## Directory Organization

```
spring-modular-monolith/
├── src/main/java/                         # Main source code
│   └── com/sivalabs/bookstore/           # Root package
│       ├── BookStoreApplication.java    # Main application entry point
│       ├── config/                      # Global configuration (open module)
│       │   ├── DatabaseConfig.java
│       │   ├── MessagingConfig.java
│       │   └── WebConfig.java
│       ├── common/                      # Shared utilities (open module)  
│       │   ├── events/                  # Common event definitions
│       │   ├── exceptions/              # Shared exception classes
│       │   └── utils/                   # Utility classes
│       ├── catalog/                     # Product catalog module
│       │   ├── package-info.java       # Module boundary definition
│       │   ├── domain/                  # Domain entities and services
│       │   │   ├── ProductEntity.java
│       │   │   ├── ProductService.java
│       │   │   └── ProductRepository.java
│       │   ├── api/                     # Public API for cross-module access
│       │   │   └── ProductApi.java
│       │   └── web/                     # Web controllers (internal)
│       │       └── ProductController.java
│       ├── orders/                      # Order management module
│       │   ├── package-info.java       # Module boundary definition
│       │   ├── domain/                  # Core domain logic
│       │   │   ├── OrderEntity.java
│       │   │   ├── OrderService.java
│       │   │   ├── OrderRepository.java
│       │   │   └── models/              # Domain models and events
│       │   │       ├── Customer.java
│       │   │       ├── OrderItem.java
│       │   │       ├── OrderStatus.java
│       │   │       └── OrderCreatedEvent.java
│       │   ├── api/                     # Public API for cross-module access
│       │   │   └── OrderApi.java
│       │   └── web/                     # Web controllers (internal)
│       │       ├── OrderWebController.java
│       │       └── OrderRestController.java
│       ├── inventory/                   # Stock management module
│       │   ├── package-info.java       # Module boundary definition
│       │   ├── domain/                  # Domain entities and services
│       │   │   ├── InventoryEntity.java
│       │   │   ├── InventoryService.java
│       │   │   └── InventoryRepository.java
│       │   ├── api/                     # Public API for cross-module access
│       │   │   └── InventoryApi.java
│       │   └── events/                  # Event listeners
│       │       └── OrderEventHandler.java
│       └── notifications/               # Notification module
│           ├── package-info.java       # Module boundary definition
│           ├── domain/                  # Notification services
│           │   └── NotificationService.java
│           └── events/                  # Event listeners
│               └── OrderEventHandler.java
├── src/main/resources/                   # Resources and configuration
│   ├── application.properties          # Main application properties
│   ├── application-dev.properties      # Development profile
│   ├── application-prod.properties     # Production profile
│   ├── db/migration/                   # Flyway database migrations
│   │   ├── common/                     # Shared schema migrations
│   │   ├── catalog/                    # Catalog module migrations
│   │   ├── orders/                     # Orders module migrations
│   │   └── inventory/                  # Inventory module migrations
│   ├── templates/                      # Thymeleaf templates
│   │   ├── layout/                     # Common layout templates
│   │   ├── products/                   # Product-related templates
│   │   └── orders/                     # Order-related templates
│   └── static/                         # Static web assets
│       ├── css/                        # Stylesheets
│       ├── js/                         # JavaScript files
│       └── images/                     # Static images
├── src/test/java/                       # Test source code
│   └── com/sivalabs/bookstore/         # Test package structure (mirrors main)
│       ├── ModularityTests.java        # Spring Modulith verification tests
│       ├── catalog/                    # Catalog module tests
│       │   └── CatalogIntegrationTests.java
│       ├── orders/                     # Orders module tests
│       │   └── OrdersIntegrationTests.java
│       ├── inventory/                  # Inventory module tests
│       │   └── InventoryIntegrationTests.java
│       └── integration/                # Cross-module integration tests
│           └── OrderWorkflowTests.java
├── src/test/resources/                 # Test resources
│   ├── application-test.properties     # Test configuration
│   └── testcontainers/                 # TestContainers configuration
├── .spec-workflow/                     # Specification workflow (hidden)
│   ├── steering/                       # Project steering documents
│   │   ├── product.md
│   │   ├── tech.md
│   │   └── structure.md
│   └── specs/                          # Feature specifications
│       └── hazelcast-cache-integration/
│           ├── requirements.md
│           ├── design.md
│           └── tasks.md
├── k8s/                               # Kubernetes deployment manifests
│   ├── namespace.yaml
│   ├── deployment.yaml
│   ├── service.yaml
│   └── configmap.yaml
├── docker/                            # Docker configuration
│   └── docker-compose.yml            # Local development services
├── docs/                              # Project documentation
│   ├── architecture/                 # Architecture documentation
│   ├── api/                          # API documentation
│   └── deployment/                   # Deployment guides
├── scripts/                           # Build and utility scripts
│   ├── build.sh
│   └── deploy.sh
├── Taskfile.yml                      # Go-Task configuration
├── pom.xml                           # Maven project configuration
├── .mvn/wrapper/                     # Maven wrapper files
├── mvnw                              # Maven wrapper script (Unix)
├── mvnw.cmd                          # Maven wrapper script (Windows)
└── CLAUDE.md                         # Claude Code instructions
```

## Naming Conventions

### Files
- **Java Classes**: `PascalCase` (e.g., `OrderEntity`, `ProductService`, `OrderCreatedEvent`)
- **Java Packages**: `lowercase` with periods (e.g., `com.sivalabs.bookstore.orders.domain`)
- **Configuration Files**: `kebab-case` or `camelCase` (e.g., `application-dev.properties`, `docker-compose.yml`)
- **Tests**: `[ClassName]Tests` or `[ClassName]IntegrationTests` (e.g., `OrderServiceTests`, `CatalogIntegrationTests`)
- **Web Templates**: `kebab-case` (e.g., `order-details.html`, `product-list.html`)

### Code
- **Classes/Interfaces**: `PascalCase` (e.g., `OrderService`, `ProductRepository`, `OrderCreatedEvent`)
- **Methods/Functions**: `camelCase` (e.g., `createOrder()`, `findByOrderNumber()`, `publishEvent()`)
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `DEFAULT_PAGE_SIZE`, `MAX_RETRY_ATTEMPTS`)
- **Variables**: `camelCase` (e.g., `orderEntity`, `productCode`, `customerId`)
- **Spring Beans**: `camelCase` (e.g., `orderService`, `productRepository`, `hazelcastConfig`)

### Database Schema Objects
- **Tables**: `snake_case` (e.g., `orders`, `products`, `inventory_items`)
- **Columns**: `snake_case` (e.g., `order_number`, `product_code`, `created_at`)
- **Schemas**: `lowercase` (e.g., `catalog`, `orders`, `inventory`)
- **Sequences**: `[table]_id_seq` (e.g., `order_id_seq`, `product_id_seq`)

## Import Patterns

### Import Order
1. Java standard library imports
2. Third-party library imports (Spring, Jakarta, etc.)
3. Project internal imports (same module)
4. Cross-module imports (other modules' public APIs)

### Module/Package Organization
```java
// Example import structure in OrderService
import java.time.LocalDateTime;           // Java standard library
import java.util.Optional;

import org.springframework.stereotype.Service;  // Third-party libraries
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import com.sivalabs.bookstore.orders.domain.models.OrderCreatedEvent;  // Same module
import com.sivalabs.bookstore.orders.domain.OrderRepository;

import com.sivalabs.bookstore.catalog.api.ProductApi;  // Cross-module API
```

### Cross-Module Import Rules
- **Only import from other modules' public API packages** (e.g., `*.api.*`)
- **Never import internal domain classes** from other modules
- **Use events for loose coupling** instead of direct API calls when possible
- **Import Spring Modulith event classes** for event handling

## Code Structure Patterns

### Spring Modulith Module Organization
```java
// Module boundary definition in package-info.java
@ApplicationModule(allowedDependencies = {"catalog"})
package com.sivalabs.bookstore.orders;

import org.springframework.modulith.ApplicationModule;
```

### Service Class Organization
```java
// Example: OrderService.java
@Service
public class OrderService {
    // 1. Constants and static fields
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    
    // 2. Final fields (dependencies)
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    // 3. Constructor injection
    OrderService(OrderRepository orderRepository, ApplicationEventPublisher publisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = publisher;
    }
    
    // 4. Public business methods
    @Transactional
    public OrderEntity createOrder(OrderEntity orderEntity) {
        // Implementation
    }
    
    // 5. Private helper methods
    private void validateOrder(OrderEntity order) {
        // Implementation
    }
}
```

### Repository Interface Organization
```java
// Example: OrderRepository.java
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    // 1. Query methods with explicit names
    Optional<OrderEntity> findByOrderNumber(String orderNumber);
    
    // 2. Custom query methods
    @Query("SELECT o FROM OrderEntity o WHERE o.status = :status")
    List<OrderEntity> findByStatus(@Param("status") OrderStatus status);
    
    // 3. Additional finder methods
    List<OrderEntity> findAllBy(Sort sort);
}
```

### Entity Class Organization
```java
// Example: OrderEntity.java
@Entity
@Table(name = "orders", schema = "orders")
public class OrderEntity {
    // 1. JPA annotations and ID field
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_id_generator")
    private Long id;
    
    // 2. Business fields with JPA annotations
    @Column(nullable = false, unique = true)
    private String orderNumber;
    
    // 3. Constructors (default + all args)
    public OrderEntity() {}
    
    public OrderEntity(Long id, String orderNumber, ...) {
        // Implementation
    }
    
    // 4. Getters and setters
    // Standard JavaBean pattern
}
```

## Code Organization Principles

1. **Single Responsibility**: Each class serves one business purpose within its module
2. **Modularity**: Code is organized into Spring Modulith modules with clear boundaries
3. **Testability**: Services use constructor injection for easy mocking and testing
4. **Consistency**: All modules follow the same internal structure pattern
5. **API-First**: Cross-module communication through well-defined public APIs
6. **Event-Driven**: Loose coupling via domain events for module communication

## Module Boundaries

### Spring Modulith Module Rules
- **Public API**: Only classes in `*.api.*` packages are accessible to other modules
- **Domain Isolation**: Each module's domain objects are private to the module
- **Data Ownership**: Each module manages its own database schema
- **Event Publishing**: Modules communicate via Spring Modulith events
- **Dependency Direction**: Modules can only depend on explicitly allowed modules

### Cross-Module Communication Patterns
- **Synchronous API Calls**: For immediate data needs (e.g., product validation during order creation)
- **Asynchronous Events**: For business process flows (e.g., order created → inventory update)
- **Public APIs Only**: Never access internal classes of other modules
- **No Shared Entities**: Each module defines its own entity classes

### Module Dependency Rules
```java
// Orders module dependencies (in package-info.java)
@ApplicationModule(allowedDependencies = {"catalog"})
package com.sivalabs.bookstore.orders;

// Catalog module (no dependencies)
@ApplicationModule
package com.sivalabs.bookstore.catalog;

// Inventory module dependencies
@ApplicationModule(allowedDependencies = {"orders"})
package com.sivalabs.bookstore.inventory;
```

## Code Size Guidelines

### File Size Limits
- **Service Classes**: Maximum 300 lines per class
- **Entity Classes**: Maximum 200 lines per class  
- **Controller Classes**: Maximum 150 lines per class
- **Configuration Classes**: Maximum 100 lines per class

### Method Complexity
- **Service Methods**: Maximum 30 lines per method
- **Controller Methods**: Maximum 20 lines per method
- **Repository Methods**: Use Spring Data JPA conventions for brevity
- **Nesting Depth**: Maximum 3 levels of nesting

### Class Complexity
- **Cyclomatic Complexity**: Maximum 10 per method
- **Class Responsibilities**: Single responsibility principle strictly enforced
- **Constructor Parameters**: Maximum 5 parameters (use configuration objects for more)

## Testing Structure

### Test Organization Pattern
```java
// Module testing with @ApplicationModuleTest
@ApplicationModuleTest
class OrdersIntegrationTests {
    @Autowired
    private OrderService orderService;
    
    @MockBean  // Mock external module dependencies
    private ProductApi productApi;
    
    // Test methods following arrange-act-assert pattern
}
```

### Test File Naming
- **Unit Tests**: `[ClassName]Tests.java`
- **Integration Tests**: `[ModuleName]IntegrationTests.java`
- **Modularity Tests**: `ModularityTests.java` (Spring Modulith verification)
- **End-to-End Tests**: `[Workflow]E2ETests.java`

## Documentation Standards

### Code Documentation
- **Public APIs**: Complete JavaDoc with `@param`, `@return`, `@throws`
- **Service Classes**: Class-level JavaDoc explaining business purpose
- **Complex Methods**: Inline comments for business logic explanation
- **Configuration**: Comments explaining configuration choices

### Architecture Documentation
- **Module Purpose**: Each module has README explaining its responsibility
- **API Documentation**: OpenAPI/Swagger for REST endpoints
- **Event Documentation**: Document all published and consumed events
- **Database Schema**: Flyway migration files serve as schema documentation

### Spring Modulith Documentation
- **Module Dependencies**: Documented in `package-info.java` files
- **Event Flow**: Sequence diagrams for cross-module event flows
- **API Contracts**: Interface documentation for public APIs
- **Testing Strategy**: Documentation of module isolation testing approach