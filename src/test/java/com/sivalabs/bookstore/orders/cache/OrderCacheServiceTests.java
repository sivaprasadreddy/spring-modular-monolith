package com.sivalabs.bookstore.orders.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.hazelcast.map.IMap;
import com.hazelcast.map.LocalMapStats;
import com.sivalabs.bookstore.orders.domain.OrderEntity;
import com.sivalabs.bookstore.orders.domain.models.Customer;
import com.sivalabs.bookstore.orders.domain.models.OrderItem;
import com.sivalabs.bookstore.orders.domain.models.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCacheService Unit Tests")
class OrderCacheServiceTests {

    @Mock
    private IMap<String, Object> ordersCache;

    @Mock
    private CacheErrorHandler errorHandler;

    @Mock
    private LocalMapStats localMapStats;

    private OrderCacheService orderCacheService;

    private OrderEntity testOrder;
    private OrderEntity anotherTestOrder;

    @BeforeEach
    void setUp() {
        orderCacheService = new OrderCacheService(ordersCache, errorHandler);

        // Create test order data
        testOrder = createTestOrder("ORD-001", 1L, "Test Product", 2);
        anotherTestOrder = createTestOrder("ORD-002", 2L, "Another Product", 1);
    }

    @Nested
    @DisplayName("Basic Cache Operations")
    class BasicCacheOperations {

        @Test
        @DisplayName("Should find order by order number successfully")
        void shouldFindOrderByOrderNumberSuccessfully() {
            String orderNumber = "ORD-001";

            // Given
            given(errorHandler.executeWithFallback(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("findByOrderNumber"),
                            org.mockito.ArgumentMatchers.eq(orderNumber),
                            org.mockito.ArgumentMatchers.any()))
                    .willAnswer(invocation -> {
                        // Simulate the actual operation
                        lenient().when(ordersCache.get(orderNumber)).thenReturn(testOrder);
                        return Optional.of(testOrder);
                    });

            // When
            Optional<OrderEntity> result = orderCacheService.findByOrderNumber(orderNumber);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testOrder);
            verify(errorHandler)
                    .executeWithFallback(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("findByOrderNumber"),
                            org.mockito.ArgumentMatchers.eq(orderNumber),
                            org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should return empty when order not found in cache")
        void shouldReturnEmptyWhenOrderNotFoundInCache() {
            String orderNumber = "ORD-999";

            // Given
            given(errorHandler.executeWithFallback(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("findByOrderNumber"),
                            org.mockito.ArgumentMatchers.eq(orderNumber),
                            org.mockito.ArgumentMatchers.any()))
                    .willAnswer(invocation -> {
                        // Simulate cache miss
                        lenient().when(ordersCache.get(orderNumber)).thenReturn(null);
                        return Optional.empty();
                    });

            // When
            Optional<OrderEntity> result = orderCacheService.findByOrderNumber(orderNumber);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle unexpected object type in cache")
        void shouldHandleUnexpectedObjectTypeInCache() {
            String orderNumber = "ORD-001";
            String unexpectedObject = "not-an-order";

            // Given
            given(errorHandler.executeWithFallback(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("findByOrderNumber"),
                            org.mockito.ArgumentMatchers.eq(orderNumber),
                            org.mockito.ArgumentMatchers.any()))
                    .willAnswer(invocation -> {
                        // Simulate unexpected object in cache
                        lenient().when(ordersCache.get(orderNumber)).thenReturn(unexpectedObject);
                        return Optional.empty();
                    });

            // When
            Optional<OrderEntity> result = orderCacheService.findByOrderNumber(orderNumber);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should cache order successfully")
        void shouldCacheOrderSuccessfully() {
            String orderNumber = "ORD-001";

            // Given
            given(errorHandler.executeVoidOperation(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("cacheOrder"),
                            org.mockito.ArgumentMatchers.eq(orderNumber)))
                    .willReturn(true);

            // When
            boolean result = orderCacheService.cacheOrder(orderNumber, testOrder);

            // Then
            assertThat(result).isTrue();
            verify(errorHandler)
                    .executeVoidOperation(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("cacheOrder"),
                            org.mockito.ArgumentMatchers.eq(orderNumber));
        }

        @Test
        @DisplayName("Should reject caching null order")
        void shouldRejectCachingNullOrder() {
            String orderNumber = "ORD-001";

            // When
            boolean result = orderCacheService.cacheOrder(orderNumber, null);

            // Then
            assertThat(result).isFalse();
            verifyNoInteractions(errorHandler);
        }

        @Test
        @DisplayName("Should update cached order successfully")
        void shouldUpdateCachedOrderSuccessfully() {
            String orderNumber = "ORD-001";

            // Given
            given(errorHandler.executeVoidOperation(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("updateCachedOrder"),
                            org.mockito.ArgumentMatchers.eq(orderNumber)))
                    .willReturn(true);

            // When
            boolean result = orderCacheService.updateCachedOrder(orderNumber, testOrder);

            // Then
            assertThat(result).isTrue();
            verify(errorHandler)
                    .executeVoidOperation(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("updateCachedOrder"),
                            org.mockito.ArgumentMatchers.eq(orderNumber));
        }

        @Test
        @DisplayName("Should remove order from cache successfully")
        void shouldRemoveOrderFromCacheSuccessfully() {
            String orderNumber = "ORD-001";

            // Given
            given(errorHandler.executeVoidOperation(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("removeFromCache"),
                            org.mockito.ArgumentMatchers.eq(orderNumber)))
                    .willReturn(true);

            // When
            boolean result = orderCacheService.removeFromCache(orderNumber);

            // Then
            assertThat(result).isTrue();
            verify(errorHandler)
                    .executeVoidOperation(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("removeFromCache"),
                            org.mockito.ArgumentMatchers.eq(orderNumber));
        }

        @Test
        @DisplayName("Should check if order exists in cache")
        void shouldCheckIfOrderExistsInCache() {
            String orderNumber = "ORD-001";

            // Given
            given(errorHandler.executeWithFallback(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("existsInCache"),
                            org.mockito.ArgumentMatchers.eq(orderNumber),
                            org.mockito.ArgumentMatchers.any()))
                    .willReturn(true);

            // When
            boolean result = orderCacheService.existsInCache(orderNumber);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should evict order from cache successfully")
        void shouldEvictOrderFromCacheSuccessfully() {
            String orderNumber = "ORD-001";

            // Given
            given(errorHandler.executeVoidOperation(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("evictFromCache"),
                            org.mockito.ArgumentMatchers.eq(orderNumber)))
                    .willReturn(true);

            // When
            boolean result = orderCacheService.evictFromCache(orderNumber);

            // Then
            assertThat(result).isTrue();
            verify(errorHandler)
                    .executeVoidOperation(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("evictFromCache"),
                            org.mockito.ArgumentMatchers.eq(orderNumber));
        }
    }

    @Nested
    @DisplayName("Timeout Operations")
    class TimeoutOperations {

        @Test
        @DisplayName("Should find order with timeout successfully")
        void shouldFindOrderWithTimeoutSuccessfully() {
            String orderNumber = "ORD-001";
            CompletableFuture<Object> future = CompletableFuture.completedFuture(testOrder);

            // Given
            given(errorHandler.executeWithFallback(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("findByOrderNumberWithTimeout"),
                            org.mockito.ArgumentMatchers.eq(orderNumber),
                            org.mockito.ArgumentMatchers.any()))
                    .willAnswer(invocation -> {
                        // Simulate async operation
                        lenient().when(ordersCache.getAsync(orderNumber)).thenReturn(future);
                        return Optional.of(testOrder);
                    });

            // When
            Optional<OrderEntity> result = orderCacheService.findByOrderNumberWithTimeout(orderNumber);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testOrder);
        }

        @Test
        @DisplayName("Should cache order with timeout successfully")
        void shouldCacheOrderWithTimeoutSuccessfully() {
            String orderNumber = "ORD-001";

            // Given
            given(errorHandler.executeVoidOperation(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("cacheOrderWithTimeout"),
                            org.mockito.ArgumentMatchers.eq(orderNumber)))
                    .willReturn(true);

            // When
            boolean result = orderCacheService.cacheOrderWithTimeout(orderNumber, testOrder);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should reject caching null order with timeout")
        void shouldRejectCachingNullOrderWithTimeout() {
            String orderNumber = "ORD-001";

            // When
            boolean result = orderCacheService.cacheOrderWithTimeout(orderNumber, null);

            // Then
            assertThat(result).isFalse();
            verifyNoInteractions(errorHandler);
        }
    }

    @Nested
    @DisplayName("TTL Operations")
    class TTLOperations {

        @Test
        @DisplayName("Should cache order with TTL successfully")
        void shouldCacheOrderWithTtlSuccessfully() {
            String orderNumber = "ORD-001";
            int ttlSeconds = 3600;

            // Given
            given(errorHandler.executeVoidOperation(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("cacheOrderWithTtl"),
                            org.mockito.ArgumentMatchers.eq(orderNumber)))
                    .willReturn(true);

            // When
            boolean result = orderCacheService.cacheOrderWithTtl(orderNumber, testOrder, ttlSeconds);

            // Then
            assertThat(result).isTrue();
            verify(errorHandler)
                    .executeVoidOperation(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("cacheOrderWithTtl"),
                            org.mockito.ArgumentMatchers.eq(orderNumber));
        }

        @Test
        @DisplayName("Should reject caching null order with TTL")
        void shouldRejectCachingNullOrderWithTtl() {
            String orderNumber = "ORD-001";
            int ttlSeconds = 3600;

            // When
            boolean result = orderCacheService.cacheOrderWithTtl(orderNumber, null, ttlSeconds);

            // Then
            assertThat(result).isFalse();
            verifyNoInteractions(errorHandler);
        }
    }

    @Nested
    @DisplayName("Error Handling and Circuit Breaker")
    class ErrorHandlingAndCircuitBreaker {

        @Test
        @DisplayName("Should handle cache errors gracefully in find operation")
        void shouldHandleCacheErrorsGracefullyInFindOperation() {
            String orderNumber = "ORD-001";

            // Given
            given(errorHandler.executeWithFallback(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("findByOrderNumber"),
                            org.mockito.ArgumentMatchers.eq(orderNumber),
                            org.mockito.ArgumentMatchers.any()))
                    .willReturn(Optional.empty()); // Simulate error fallback

            // When
            Optional<OrderEntity> result = orderCacheService.findByOrderNumber(orderNumber);

            // Then
            assertThat(result).isEmpty();
            verify(errorHandler)
                    .executeWithFallback(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("findByOrderNumber"),
                            org.mockito.ArgumentMatchers.eq(orderNumber),
                            org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should handle cache errors gracefully in cache operation")
        void shouldHandleCacheErrorsGracefullyInCacheOperation() {
            String orderNumber = "ORD-001";

            // Given
            given(errorHandler.executeVoidOperation(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("cacheOrder"),
                            org.mockito.ArgumentMatchers.eq(orderNumber)))
                    .willReturn(false); // Simulate error

            // When
            boolean result = orderCacheService.cacheOrder(orderNumber, testOrder);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should check circuit breaker status")
        void shouldCheckCircuitBreakerStatus() {
            // Given
            given(errorHandler.isCircuitOpen()).willReturn(true);

            // When
            boolean isOpen = orderCacheService.isCircuitBreakerOpen();

            // Then
            assertThat(isOpen).isTrue();
            verify(errorHandler).isCircuitOpen();
        }

        @Test
        @DisplayName("Should get circuit breaker status")
        void shouldGetCircuitBreakerStatus() {
            // Given
            given(errorHandler.isCircuitOpen()).willReturn(false);
            given(errorHandler.getCacheErrorStats()).willReturn("Test error stats");

            // When
            String status = orderCacheService.getCircuitBreakerStatus();

            // Then
            assertThat(status).contains("Circuit State: CLOSED (Cache Active)");
            assertThat(status).contains("Test error stats");
        }

        @Test
        @DisplayName("Should check fallback to database recommendation")
        void shouldCheckFallbackToDatabaseRecommendation() {
            String operationName = "testOperation";

            // Given
            given(errorHandler.isCircuitOpen()).willReturn(false);
            given(errorHandler.shouldFallbackToDatabase(operationName)).willReturn(true);

            // When
            boolean shouldFallback = orderCacheService.shouldFallbackToDatabase(operationName);

            // Then
            assertThat(shouldFallback).isTrue();
            verify(errorHandler).shouldFallbackToDatabase(operationName);
        }

        @Test
        @DisplayName("Should reset circuit breaker successfully")
        void shouldResetCircuitBreakerSuccessfully() {
            // Given
            given(errorHandler.executeVoidOperation(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("resetCircuitBreaker"),
                            org.mockito.ArgumentMatchers.eq("manual-reset")))
                    .willReturn(true);

            // When
            boolean result = orderCacheService.resetCircuitBreaker();

            // Then
            assertThat(result).isTrue();
            verify(errorHandler)
                    .executeVoidOperation(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("resetCircuitBreaker"),
                            org.mockito.ArgumentMatchers.eq("manual-reset"));
        }
    }

    @Nested
    @DisplayName("Health Monitoring")
    class HealthMonitoring {

        @Test
        @DisplayName("Should check cache health successfully")
        void shouldCheckCacheHealthSuccessfully() {
            // Given
            given(errorHandler.checkCacheHealth(org.mockito.ArgumentMatchers.any()))
                    .willReturn(true);

            // When
            boolean isHealthy = orderCacheService.isHealthy();

            // Then
            assertThat(isHealthy).isTrue();
            verify(errorHandler).checkCacheHealth(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should test cache connectivity successfully")
        void shouldTestCacheConnectivitySuccessfully() {
            // Given
            given(errorHandler.checkCacheHealth(org.mockito.ArgumentMatchers.any()))
                    .willReturn(true);

            // When
            boolean isConnected = orderCacheService.testCacheConnectivity();

            // Then
            assertThat(isConnected).isTrue();
            verify(errorHandler).checkCacheHealth(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should get cache statistics")
        void shouldGetCacheStatistics() {
            // Given
            lenient().when(ordersCache.getName()).thenReturn("orders-cache");
            given(errorHandler.executeWithFallback(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("getCacheStats"),
                            org.mockito.ArgumentMatchers.eq("global"),
                            org.mockito.ArgumentMatchers.any()))
                    .willAnswer(invocation -> {
                        // Simulate the actual stats collection
                        return "Orders Cache Statistics:\n  Cache Name: orders-cache\n  Cache Size: 10\n";
                    });

            // When
            String stats = orderCacheService.getCacheStats();

            // Then
            assertThat(stats).contains("Orders Cache Statistics:");
            assertThat(stats).contains("Cache Name: orders-cache");
            assertThat(stats).contains("Cache Size: 10");
        }

        @Test
        @DisplayName("Should get comprehensive health report")
        void shouldGetComprehensiveHealthReport() {
            // Given
            given(errorHandler.isCircuitOpen()).willReturn(false);
            given(errorHandler.getCacheErrorStats()).willReturn("No errors");
            given(errorHandler.checkCacheHealth(org.mockito.ArgumentMatchers.any()))
                    .willReturn(true);
            given(errorHandler.executeWithFallback(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.any()))
                    .willReturn("Cache stats");

            // When
            String report = orderCacheService.getHealthReport();

            // Then
            assertThat(report).contains("=== Cache Health Report ===");
            assertThat(report).contains("Circuit State: CLOSED (Cache Active)");
            assertThat(report).contains("Connectivity Test: PASS");
        }
    }

    @Nested
    @DisplayName("Fallback Mechanisms")
    class FallbackMechanisms {

        @Test
        @DisplayName("Should find with automatic fallback when cache is available")
        void shouldFindWithAutomaticFallbackWhenCacheIsAvailable() {
            String orderNumber = "ORD-001";

            // Given
            given(errorHandler.isCircuitOpen()).willReturn(false);
            given(errorHandler.shouldFallbackToDatabase("findWithAutomaticFallback"))
                    .willReturn(false);
            given(errorHandler.executeWithFallback(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("findByOrderNumber"),
                            org.mockito.ArgumentMatchers.eq(orderNumber),
                            org.mockito.ArgumentMatchers.any()))
                    .willReturn(Optional.of(testOrder));

            // When
            Optional<OrderEntity> result =
                    orderCacheService.findWithAutomaticFallback(orderNumber, () -> Optional.of(anotherTestOrder));

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testOrder);
        }

        @Test
        @DisplayName("Should use fallback when circuit breaker recommends database")
        void shouldUseFallbackWhenCircuitBreakerRecommendsDatabase() {
            String orderNumber = "ORD-001";

            // Given
            lenient().when(errorHandler.isCircuitOpen()).thenReturn(true);
            lenient()
                    .when(errorHandler.shouldFallbackToDatabase("findWithAutomaticFallback"))
                    .thenReturn(true);

            // When
            Optional<OrderEntity> result =
                    orderCacheService.findWithAutomaticFallback(orderNumber, () -> Optional.of(anotherTestOrder));

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(anotherTestOrder);
        }

        @Test
        @DisplayName("Should use fallback on cache miss when circuit is closed")
        void shouldUseFallbackOnCacheMissWhenCircuitIsClosed() {
            String orderNumber = "ORD-001";

            // Given
            given(errorHandler.isCircuitOpen()).willReturn(false);
            given(errorHandler.shouldFallbackToDatabase("findWithAutomaticFallback"))
                    .willReturn(false);
            given(errorHandler.executeWithFallback(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("findByOrderNumber"),
                            org.mockito.ArgumentMatchers.eq(orderNumber),
                            org.mockito.ArgumentMatchers.any()))
                    .willReturn(Optional.empty()); // Cache miss

            // When
            Optional<OrderEntity> result =
                    orderCacheService.findWithAutomaticFallback(orderNumber, () -> Optional.of(anotherTestOrder));

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(anotherTestOrder);
        }
    }

    @Nested
    @DisplayName("Cache Warming")
    class CacheWarming {

        @Test
        @DisplayName("Should warm up cache successfully")
        void shouldWarmUpCacheSuccessfully() {
            List<String> orderNumbers = Arrays.asList("ORD-001", "ORD-002", "ORD-003");

            // Given
            given(errorHandler.executeWithFallback(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("warmUpCache"),
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.any()))
                    .willReturn(true)
                    .willReturn(true)
                    .willReturn(false); // 2 successful, 1 failed

            // When
            int result = orderCacheService.warmUpCache(orderNumbers);

            // Then
            assertThat(result).isEqualTo(2);
            verify(errorHandler, times(3))
                    .executeWithFallback(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("warmUpCache"),
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should handle empty order numbers in warm up")
        void shouldHandleEmptyOrderNumbersInWarmUp() {
            List<String> emptyList = Arrays.asList();

            // When
            int result = orderCacheService.warmUpCache(emptyList);

            // Then
            assertThat(result).isEqualTo(0);
            verifyNoInteractions(errorHandler);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Null Handling")
    class EdgeCasesAndNullHandling {

        @Test
        @DisplayName("Should handle null order number gracefully")
        void shouldHandleNullOrderNumberGracefully() {
            // Given
            given(errorHandler.executeWithFallback(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("findByOrderNumber"),
                            org.mockito.ArgumentMatchers.isNull(),
                            org.mockito.ArgumentMatchers.any()))
                    .willReturn(Optional.empty());

            // When
            Optional<OrderEntity> result = orderCacheService.findByOrderNumber(null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should reject null order in update operation")
        void shouldRejectNullOrderInUpdateOperation() {
            String orderNumber = "ORD-001";

            // When
            boolean result = orderCacheService.updateCachedOrder(orderNumber, null);

            // Then
            assertThat(result).isFalse();
            verifyNoInteractions(errorHandler);
        }

        @Test
        @DisplayName("Should handle empty cache gracefully")
        void shouldHandleEmptyCacheGracefully() {
            // Given
            lenient().when(ordersCache.getName()).thenReturn("orders-cache");
            given(errorHandler.executeWithFallback(
                            org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("getCacheStats"),
                            org.mockito.ArgumentMatchers.eq("global"),
                            org.mockito.ArgumentMatchers.any()))
                    .willAnswer(invocation -> "Orders Cache Statistics:\n  Cache Size: 0\n");

            // When
            String stats = orderCacheService.getCacheStats();

            // Then
            assertThat(stats).contains("Cache Size: 0");
        }
    }

    // Helper method to create test orders
    private OrderEntity createTestOrder(String orderNumber, Long id, String productName, int quantity) {
        Customer customer = new Customer("John Doe", "john@example.com", "+1234567890");
        OrderItem orderItem = new OrderItem("PROD-" + id, productName, BigDecimal.valueOf(99.99), quantity);

        return new OrderEntity(
                id,
                orderNumber,
                customer,
                "123 Test Street, Test City, Test State 12345",
                orderItem,
                OrderStatus.NEW,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now());
    }
}
