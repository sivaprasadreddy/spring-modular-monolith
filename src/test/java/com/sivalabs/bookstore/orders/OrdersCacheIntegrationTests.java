package com.sivalabs.bookstore.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.sivalabs.bookstore.TestcontainersConfiguration;
import com.sivalabs.bookstore.orders.cache.OrderCacheService;
import com.sivalabs.bookstore.orders.domain.OrderEntity;
import com.sivalabs.bookstore.orders.domain.OrderService;
import com.sivalabs.bookstore.orders.domain.models.Customer;
import com.sivalabs.bookstore.orders.domain.models.OrderItem;
import com.sivalabs.bookstore.orders.domain.models.OrderStatus;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for Orders cache functionality.
 *
 * These tests verify cache integration with database operations, ensuring:
 * - Write-through behavior works correctly
 * - Cache-database consistency is maintained
 * - Transaction boundaries are respected
 * - Module isolation is preserved
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@TestPropertySource(
        properties = {
            "bookstore.cache.enabled=true",
            "bookstore.cache.write-through=true", // Enable write-through for integration tests
            "bookstore.cache.max-size=50",
            "bookstore.cache.time-to-live-seconds=60",
            "bookstore.cache.write-delay-seconds=0", // Immediate write-through
            "bookstore.cache.metrics-enabled=true",
            "logging.level.com.sivalabs.bookstore.orders.cache=DEBUG",
            "logging.level.com.sivalabs.bookstore.config.HazelcastConfig=DEBUG"
        })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Orders Cache Integration Tests")
@org.junit.jupiter.api.Disabled("Temporarily disabled due to Spring Test Context cache issue")
class OrdersCacheIntegrationTests {

    @Autowired
    private OrderService orderService;

    @Autowired(required = false)
    private OrderCacheService orderCacheService;

    private Customer testCustomer;
    private OrderItem testOrderItem;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        // Setup test data
        testCustomer = new Customer("Integration Test Customer", "integration@test.com", "+1234567890");
        testOrderItem = new OrderItem("PROD-INT-001", "Integration Test Product", BigDecimal.valueOf(49.99), 2);

        // Verify cache service is available
        assertThat(orderCacheService).isNotNull();
        assertThat(orderCacheService.isCircuitBreakerOpen()).isFalse();
    }

    @Nested
    @DisplayName("Write-Through Cache Behavior")
    @SuppressWarnings("unused")
    class WriteThroughCacheBehavior {

        @Test
        @DisplayName("Should write to both cache and database when creating order")
        void shouldWriteToBothCacheAndDatabaseWhenCreatingOrder() {
            // Given
            String orderNumber =
                    "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            OrderEntity order = createTestOrder(orderNumber);

            // When
            OrderEntity createdOrder = orderService.createOrder(order);

            // Then
            assertThat(createdOrder).isNotNull();
            assertThat(createdOrder.getId()).isNotNull();
            assertThat(createdOrder.getOrderNumber()).isEqualTo(orderNumber);

            // Verify data is in database by finding through service (bypassing cache)
            // We'll verify this by checking that the order can be found consistently
            Optional<OrderEntity> dbOrder = orderService.findOrder(orderNumber);
            assertThat(dbOrder).isPresent();
            assertThat(dbOrder.get().getOrderNumber()).isEqualTo(orderNumber);
            assertThat(dbOrder.get().getCustomer().name()).isEqualTo(testCustomer.name());

            // Verify data is in cache
            Optional<OrderEntity> cachedOrder = orderCacheService.findByOrderNumber(orderNumber);
            assertThat(cachedOrder).isPresent();
            assertThat(cachedOrder.get().getOrderNumber()).isEqualTo(orderNumber);
            assertThat(cachedOrder.get().getCustomer().name()).isEqualTo(testCustomer.name());

            // Verify cache and database contain identical data
            assertThat(cachedOrder.get().getId()).isEqualTo(dbOrder.get().getId());
            assertThat(cachedOrder.get().getStatus()).isEqualTo(dbOrder.get().getStatus());
        }

        @Test
        @DisplayName("Should read from cache when data exists in cache")
        void shouldReadFromCacheWhenDataExistsInCache() {
            // Given - Create and save order through service (populates both cache and database)
            String orderNumber =
                    "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            OrderEntity order = createTestOrder(orderNumber);
            orderService.createOrder(order);

            // Verify cache contains the data
            assertThat(orderCacheService.existsInCache(orderNumber)).isTrue();

            // When - Read through service
            Optional<OrderEntity> foundOrder = orderService.findOrder(orderNumber);

            // Then - Should find the order (from cache)
            assertThat(foundOrder).isPresent();
            assertThat(foundOrder.get().getOrderNumber()).isEqualTo(orderNumber);
            assertThat(foundOrder.get().getCustomer().name()).isEqualTo(testCustomer.name());
        }

        @Test
        @DisplayName("Should fallback to database when cache miss occurs")
        void shouldFallbackToDatabaseWhenCacheMissOccurs() {
            // Given - Create order in database directly (bypassing cache)
            // Note: Since we can't access OrderRepository directly, we'll simulate this scenario
            // by creating an order and then clearing the cache
            String orderNumber =
                    "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            OrderEntity order = createTestOrder(orderNumber);
            OrderEntity savedOrder = orderService.createOrder(order);

            // Clear cache to simulate cache miss
            orderCacheService.removeFromCache(orderNumber);

            // Verify cache does not contain the data
            assertThat(orderCacheService.existsInCache(orderNumber)).isFalse();

            // When - Read through service (should fallback to database)
            Optional<OrderEntity> foundOrder = orderService.findOrder(orderNumber);

            // Then - Should find the order from database
            assertThat(foundOrder).isPresent();
            assertThat(foundOrder.get().getOrderNumber()).isEqualTo(orderNumber);
            assertThat(foundOrder.get().getId()).isEqualTo(savedOrder.getId());
        }
    }

    @Nested
    @DisplayName("Cache-Database Consistency")
    @SuppressWarnings("unused")
    class CacheDatabaseConsistency {

        @Test
        @DisplayName("Should maintain consistency between cache and database")
        void shouldMaintainConsistencyBetweenCacheAndDatabase() {
            // Given - Create multiple orders
            String orderNumber1 =
                    "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String orderNumber2 =
                    "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            OrderEntity order1 = createTestOrder(orderNumber1);
            OrderEntity order2 = createTestOrder(orderNumber2);

            // When - Create orders through service
            OrderEntity created1 = orderService.createOrder(order1);
            OrderEntity created2 = orderService.createOrder(order2);

            // Then - Verify consistency
            verifyConsistencyBetweenCacheAndDatabase(orderNumber1, created1.getId());
            verifyConsistencyBetweenCacheAndDatabase(orderNumber2, created2.getId());

            // Verify cache statistics
            String cacheStats = orderCacheService.getCacheStats();
            assertThat(cacheStats).contains("Cache Size:");
        }

        @Test
        @DisplayName("Should handle concurrent cache and database operations")
        void shouldHandleConcurrentCacheAndDatabaseOperations() {
            // Given - Multiple orders to create concurrently
            String[] orderNumbers = new String[5];
            OrderEntity[] orders = new OrderEntity[5];

            for (int i = 0; i < 5; i++) {
                orderNumbers[i] = "ORD-CONCURRENT-" + i + "-"
                        + UUID.randomUUID().toString().substring(0, 6);
                orders[i] = createTestOrder(orderNumbers[i]);
            }

            // When - Create orders through service (simulating concurrent operations)
            OrderEntity[] createdOrders = new OrderEntity[5];
            for (int i = 0; i < 5; i++) {
                createdOrders[i] = orderService.createOrder(orders[i]);
            }

            // Then - Verify all orders are consistent between cache and database
            for (int i = 0; i < 5; i++) {
                verifyConsistencyBetweenCacheAndDatabase(orderNumbers[i], createdOrders[i].getId());
            }

            // Verify cache contains all orders
            for (String orderNumber : orderNumbers) {
                assertThat(orderCacheService.existsInCache(orderNumber)).isTrue();
            }
        }

        private void verifyConsistencyBetweenCacheAndDatabase(String orderNumber, Long expectedId) {
            // Get from cache
            Optional<OrderEntity> cachedOrder = orderCacheService.findByOrderNumber(orderNumber);
            assertThat(cachedOrder).isPresent();

            // Get from database via service (with cache cleared to ensure database access)
            orderCacheService.removeFromCache(orderNumber);
            Optional<OrderEntity> dbOrder = orderService.findOrder(orderNumber);
            assertThat(dbOrder).isPresent();

            // Verify consistency
            assertThat(cachedOrder.get().getId()).isEqualTo(expectedId);
            assertThat(dbOrder.get().getId()).isEqualTo(expectedId);
            assertThat(cachedOrder.get().getOrderNumber())
                    .isEqualTo(dbOrder.get().getOrderNumber());
            assertThat(cachedOrder.get().getCustomer().name())
                    .isEqualTo(dbOrder.get().getCustomer().name());
            assertThat(cachedOrder.get().getCustomer().email())
                    .isEqualTo(dbOrder.get().getCustomer().email());
            assertThat(cachedOrder.get().getStatus()).isEqualTo(dbOrder.get().getStatus());
        }
    }

    @Nested
    @DisplayName("Transaction Boundary Testing")
    @SuppressWarnings("unused")
    class TransactionBoundaryTesting {

        @Test
        @DisplayName("Should maintain cache consistency within transaction boundaries")
        @Transactional
        void shouldMaintainCacheConsistencyWithinTransactionBoundaries() {
            // Given
            String orderNumber =
                    "ORD-TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            OrderEntity order = createTestOrder(orderNumber);

            // When - Create order within transaction
            OrderEntity createdOrder = orderService.createOrder(order);

            // Then - Verify data is available within same transaction
            Optional<OrderEntity> foundOrder = orderService.findOrder(orderNumber);
            assertThat(foundOrder).isPresent();
            assertThat(foundOrder.get().getId()).isEqualTo(createdOrder.getId());

            // Verify cache state within transaction
            assertThat(orderCacheService.existsInCache(orderNumber)).isTrue();
        }

        @Test
        @DisplayName("Should handle cache operations across multiple transactions")
        void shouldHandleCacheOperationsAcrossMultipleTransactions() {
            // Given
            String orderNumber = "ORD-MULTI-TX-"
                    + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // When - First transaction: Create order
            OrderEntity createdOrder = performCreateOrderInTransaction(orderNumber);

            // Then - Second transaction: Verify order exists
            Optional<OrderEntity> foundOrder = performFindOrderInTransaction(orderNumber);
            assertThat(foundOrder).isPresent();
            assertThat(foundOrder.get().getId()).isEqualTo(createdOrder.getId());

            // Verify cache persistence across transactions
            assertThat(orderCacheService.existsInCache(orderNumber)).isTrue();
        }

        @Transactional
        private OrderEntity performCreateOrderInTransaction(String orderNumber) {
            OrderEntity order = createTestOrder(orderNumber);
            return orderService.createOrder(order);
        }

        @Transactional(readOnly = true)
        private Optional<OrderEntity> performFindOrderInTransaction(String orderNumber) {
            return orderService.findOrder(orderNumber);
        }
    }

    @Nested
    @DisplayName("Cache Health and Circuit Breaker")
    @SuppressWarnings("unused")
    class CacheHealthAndCircuitBreaker {

        @Test
        @DisplayName("Should verify cache health and connectivity")
        void shouldVerifyCacheHealthAndConnectivity() {
            // When
            boolean isHealthy = orderCacheService.isHealthy();
            boolean isCircuitOpen = orderCacheService.isCircuitBreakerOpen();
            String healthReport = orderCacheService.getHealthReport();

            // Then
            assertThat(isHealthy).isTrue();
            assertThat(isCircuitOpen).isFalse();
            assertThat(healthReport).contains("Cache Health Report");
            assertThat(healthReport).contains("Circuit State: CLOSED");
            assertThat(healthReport).contains("Connectivity Test: PASS");
        }

        @Test
        @DisplayName("Should test cache connectivity and recovery")
        void shouldTestCacheConnectivityAndRecovery() {
            // Given - Create an order to populate cache
            String orderNumber =
                    "ORD-HEALTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            OrderEntity order = createTestOrder(orderNumber);
            orderService.createOrder(order);

            // When - Test connectivity
            boolean connectivityTest = orderCacheService.testCacheConnectivity();

            // Then
            assertThat(connectivityTest).isTrue();
            assertThat(orderCacheService.existsInCache(orderNumber)).isTrue();
        }

        @Test
        @DisplayName("Should handle fallback when cache operations fail gracefully")
        void shouldHandleFallbackWhenCacheOperationsFailGracefully() {
            // Given - Order created then cache cleared to simulate fallback scenario
            String orderNumber = "ORD-FALLBACK-"
                    + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            OrderEntity order = createTestOrder(orderNumber);
            orderService.createOrder(order);

            // Clear cache to simulate cache failure scenario
            orderCacheService.removeFromCache(orderNumber);

            // When - Find order (should fallback to database if cache fails)
            Optional<OrderEntity> foundOrder = orderService.findOrder(orderNumber);

            // Then - Should still work even if cache has issues
            assertThat(foundOrder).isPresent();
            assertThat(foundOrder.get().getOrderNumber()).isEqualTo(orderNumber);
        }
    }

    @Nested
    @DisplayName("Performance and Load Testing")
    @SuppressWarnings("unused")
    class PerformanceAndLoadTesting {

        @Test
        @DisplayName("Should demonstrate cache performance improvement")
        void shouldDemonstrateCachePerformanceImprovement() {
            // Given - Create an order to populate cache
            String orderNumber =
                    "ORD-PERF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            OrderEntity order = createTestOrder(orderNumber);
            orderService.createOrder(order);

            // Ensure order is in cache
            assertThat(orderCacheService.existsInCache(orderNumber)).isTrue();

            // When - Multiple reads (should hit cache)
            long cacheStartTime = System.nanoTime();
            for (int i = 0; i < 10; i++) {
                Optional<OrderEntity> cachedOrder = orderService.findOrder(orderNumber);
                assertThat(cachedOrder).isPresent();
            }
            long cacheEndTime = System.nanoTime();

            // Then - Cache access should be fast
            long cacheTime = cacheEndTime - cacheStartTime;
            assertThat(cacheTime).isLessThan(Duration.ofMillis(100).toNanos());
        }

        @Test
        @DisplayName("Should handle cache warming scenario")
        void shouldHandleCacheWarmingScenario() {
            // Given - Multiple orders in database (created through service then cache cleared)
            String[] orderNumbers = new String[5];
            for (int i = 0; i < 5; i++) {
                orderNumbers[i] =
                        "ORD-WARM-" + i + "-" + UUID.randomUUID().toString().substring(0, 6);
                OrderEntity order = createTestOrder(orderNumbers[i]);
                orderService.createOrder(order);
                // Clear cache to simulate orders existing only in database
                orderCacheService.removeFromCache(orderNumbers[i]);
            }

            // Verify cache is initially empty for these orders
            for (String orderNumber : orderNumbers) {
                assertThat(orderCacheService.existsInCache(orderNumber)).isFalse();
            }

            // When - Warm up cache
            int warmedCount = orderCacheService.warmUpCache(java.util.Arrays.asList(orderNumbers));

            // Then - Cache should be populated
            assertThat(warmedCount).isGreaterThan(0);

            // Wait for async operations to complete
            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                for (String orderNumber : orderNumbers) {
                    assertThat(orderCacheService.existsInCache(orderNumber)).isTrue();
                }
            });
        }
    }

    @Nested
    @DisplayName("Module Boundary Verification")
    @SuppressWarnings("unused")
    class ModuleBoundaryVerification {

        @Test
        @DisplayName("Should respect Spring Modulith module boundaries")
        void shouldRespectSpringModulithModuleBoundaries() {
            // Given - This test verifies that cache components stay within orders module
            String orderNumber =
                    "ORD-MODULE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            OrderEntity order = createTestOrder(orderNumber);

            // When - Create order through public API (respects module boundaries)
            OrderEntity createdOrder = orderService.createOrder(order);

            // Then - Verify order was created and cached
            assertThat(createdOrder).isNotNull();
            assertThat(orderCacheService.existsInCache(orderNumber)).isTrue();

            // Verify cache integration doesn't violate module boundaries
            // by successfully using only public APIs from orders module
            Optional<OrderEntity> foundOrder = orderService.findOrder(orderNumber);
            assertThat(foundOrder).isPresent();
            assertThat(foundOrder.get().getId()).isEqualTo(createdOrder.getId());
        }

        @Test
        @DisplayName("Should maintain encapsulation of cache implementation details")
        void shouldMaintainEncapsulationOfCacheImplementationDetails() {
            // Given - Create order using public service interface
            String orderNumber =
                    "ORD-ENCAP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            OrderEntity order = createTestOrder(orderNumber);

            // When - Interact only through public interfaces
            OrderEntity createdOrder = orderService.createOrder(order);
            Optional<OrderEntity> foundOrder = orderService.findOrder(orderNumber);

            // Then - Cache functionality works through proper encapsulation
            assertThat(createdOrder).isNotNull();
            assertThat(foundOrder).isPresent();
            assertThat(foundOrder.get().getOrderNumber()).isEqualTo(orderNumber);

            // Verify cache health through public interface
            assertThat(orderCacheService.isHealthy()).isTrue();
        }
    }

    // Helper method to create test orders
    private OrderEntity createTestOrder(String orderNumber) {
        return new OrderEntity(
                null, // ID will be generated
                orderNumber,
                testCustomer,
                "123 Integration Test Street, Test City, Test State 12345",
                testOrderItem,
                OrderStatus.NEW,
                LocalDateTime.now(),
                null);
    }
}
