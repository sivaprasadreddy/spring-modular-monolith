package com.sivalabs.bookstore.orders.cache;

import com.hazelcast.map.IMap;
import com.sivalabs.bookstore.orders.domain.OrderEntity;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Service providing cache operations abstraction for Order entities.
 *
 * This service acts as the primary interface for all cache operations related to orders.
 * It handles error scenarios gracefully using CacheErrorHandler and provides a clean
 * abstraction over the underlying Hazelcast IMap.
 *
 * Key features:
 * - Graceful error handling with fallback support
 * - Cache operations with timeout handling
 * - Eviction and expiry management
 * - Cache warming and preloading support
 * - Monitoring and metrics integration
 */
@Service
@ConditionalOnProperty(prefix = "bookstore.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
@Lazy
public class OrderCacheService {

    private static final Logger logger = LoggerFactory.getLogger(OrderCacheService.class);

    // Cache operation timeouts
    private static final int CACHE_READ_TIMEOUT_MS = 500;
    private static final int CACHE_WRITE_TIMEOUT_MS = 1000;

    private final IMap<String, Object> ordersCache;
    private final CacheErrorHandler errorHandler;

    public OrderCacheService(
            @Qualifier("ordersCache") IMap<String, Object> ordersCache, CacheErrorHandler errorHandler) {
        this.ordersCache = ordersCache;
        this.errorHandler = errorHandler;
        logger.info("OrderCacheService initialized with cache: {} and error handler", ordersCache.getName());
    }

    /**
     * Find an order by its order number from the cache.
     *
     * @param orderNumber the order number to search for
     * @return Optional containing the order if found in cache, empty if not found or cache error
     */
    public Optional<OrderEntity> findByOrderNumber(String orderNumber) {
        logger.debug("Looking up order in cache: {}", orderNumber);

        return errorHandler.executeWithFallback(
                () -> {
                    Object cachedValue = ordersCache.get(orderNumber);
                    if (cachedValue instanceof OrderEntity orderEntity) {
                        logger.debug("Order found in cache: {}", orderNumber);
                        return Optional.of(orderEntity);
                    } else if (cachedValue != null) {
                        logger.warn(
                                "Unexpected object type in cache for key {}: {}", orderNumber, cachedValue.getClass());
                        return Optional.empty();
                    } else {
                        logger.debug("Order not found in cache: {}", orderNumber);
                        return Optional.empty();
                    }
                },
                "findByOrderNumber",
                orderNumber,
                () -> {
                    logger.debug("Cache lookup failed for {}, returning empty", orderNumber);
                    return Optional.empty();
                });
    }

    /**
     * Find an order by its order number with timeout.
     *
     * @param orderNumber the order number to search for
     * @return Optional containing the order if found in cache, empty if not found or timeout
     */
    public Optional<OrderEntity> findByOrderNumberWithTimeout(String orderNumber) {
        logger.debug("Looking up order in cache with timeout: {}", orderNumber);

        return errorHandler.executeWithFallback(
                () -> {
                    try {
                        Object cachedValue = ordersCache
                                .getAsync(orderNumber)
                                .toCompletableFuture()
                                .get(CACHE_READ_TIMEOUT_MS, TimeUnit.MILLISECONDS);

                        if (cachedValue instanceof OrderEntity orderEntity) {
                            logger.debug("Order found in cache (with timeout): {}", orderNumber);
                            return Optional.of(orderEntity);
                        } else if (cachedValue != null) {
                            logger.warn(
                                    "Unexpected object type in cache for key {}: {}",
                                    orderNumber,
                                    cachedValue.getClass());
                            return Optional.empty();
                        } else {
                            logger.debug("Order not found in cache (with timeout): {}", orderNumber);
                            return Optional.empty();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Cache operation timeout or error", e);
                    }
                },
                "findByOrderNumberWithTimeout",
                orderNumber,
                () -> {
                    logger.debug("Cache lookup with timeout failed for {}, returning empty", orderNumber);
                    return Optional.empty();
                });
    }

    /**
     * Cache an order entity.
     * This operation will trigger write-through to the database if configured.
     *
     * @param orderNumber the order number (cache key)
     * @param order the order entity to cache
     * @return true if caching was successful, false if it failed
     */
    public boolean cacheOrder(String orderNumber, OrderEntity order) {
        if (order == null) {
            logger.warn("Attempted to cache null order for key: {}", orderNumber);
            return false;
        }

        logger.debug("Caching order: {} with ID: {}", orderNumber, order.getId());

        return errorHandler.executeVoidOperation(
                () -> {
                    ordersCache.put(orderNumber, order);
                    logger.debug("Order cached successfully: {}", orderNumber);
                },
                "cacheOrder",
                orderNumber);
    }

    /**
     * Cache an order entity with timeout.
     *
     * @param orderNumber the order number (cache key)
     * @param order the order entity to cache
     * @return true if caching was successful, false if it failed or timeout
     */
    public boolean cacheOrderWithTimeout(String orderNumber, OrderEntity order) {
        if (order == null) {
            logger.warn("Attempted to cache null order for key: {}", orderNumber);
            return false;
        }

        logger.debug("Caching order with timeout: {} with ID: {}", orderNumber, order.getId());

        return errorHandler.executeVoidOperation(
                () -> {
                    try {
                        ordersCache
                                .putAsync(orderNumber, order)
                                .toCompletableFuture()
                                .get(CACHE_WRITE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                        logger.debug("Order cached successfully with timeout: {}", orderNumber);
                    } catch (Exception e) {
                        throw new RuntimeException("Cache write timeout or error", e);
                    }
                },
                "cacheOrderWithTimeout",
                orderNumber);
    }

    /**
     * Cache an order entity with TTL (Time To Live).
     *
     * @param orderNumber the order number (cache key)
     * @param order the order entity to cache
     * @param ttlSeconds time to live in seconds
     * @return true if caching was successful, false if it failed
     */
    public boolean cacheOrderWithTtl(String orderNumber, OrderEntity order, int ttlSeconds) {
        if (order == null) {
            logger.warn("Attempted to cache null order for key: {}", orderNumber);
            return false;
        }

        logger.debug("Caching order with TTL: {} seconds for key: {}", ttlSeconds, orderNumber);

        return errorHandler.executeVoidOperation(
                () -> {
                    ordersCache.put(orderNumber, order, ttlSeconds, TimeUnit.SECONDS);
                    logger.debug("Order cached with TTL successfully: {}", orderNumber);
                },
                "cacheOrderWithTtl",
                orderNumber);
    }

    /**
     * Update an existing cached order.
     *
     * @param orderNumber the order number (cache key)
     * @param order the updated order entity
     * @return true if update was successful, false if it failed
     */
    public boolean updateCachedOrder(String orderNumber, OrderEntity order) {
        if (order == null) {
            logger.warn("Attempted to update cache with null order for key: {}", orderNumber);
            return false;
        }

        logger.debug("Updating cached order: {}", orderNumber);

        return errorHandler.executeVoidOperation(
                () -> {
                    // Use replace to only update if the key already exists
                    Object previous = ordersCache.replace(orderNumber, order);
                    if (previous != null) {
                        logger.debug("Cached order updated successfully: {}", orderNumber);
                    } else {
                        logger.debug("Order not in cache, performing regular put: {}", orderNumber);
                        ordersCache.put(orderNumber, order);
                    }
                },
                "updateCachedOrder",
                orderNumber);
    }

    /**
     * Remove an order from the cache.
     *
     * @param orderNumber the order number to remove
     * @return true if removal was successful, false if it failed
     */
    public boolean removeFromCache(String orderNumber) {
        logger.debug("Removing order from cache: {}", orderNumber);

        return errorHandler.executeVoidOperation(
                () -> {
                    Object removed = ordersCache.remove(orderNumber);
                    if (removed != null) {
                        logger.debug("Order removed from cache successfully: {}", orderNumber);
                    } else {
                        logger.debug("Order not in cache for removal: {}", orderNumber);
                    }
                },
                "removeFromCache",
                orderNumber);
    }

    /**
     * Check if an order exists in the cache.
     *
     * @param orderNumber the order number to check
     * @return true if the order exists in cache, false otherwise
     */
    public boolean existsInCache(String orderNumber) {
        return errorHandler.executeWithFallback(
                () -> {
                    boolean exists = ordersCache.containsKey(orderNumber);
                    logger.debug("Cache existence check for {}: {}", orderNumber, exists);
                    return exists;
                },
                "existsInCache",
                orderNumber,
                () -> {
                    logger.debug("Cache existence check failed for {}, assuming false", orderNumber);
                    return false;
                });
    }

    /**
     * Evict an order from the cache only (without triggering database operations).
     *
     * @param orderNumber the order number to evict
     * @return true if eviction was successful, false if it failed
     */
    public boolean evictFromCache(String orderNumber) {
        logger.debug("Evicting order from cache: {}", orderNumber);

        return errorHandler.executeVoidOperation(
                () -> {
                    ordersCache.evict(orderNumber);
                    logger.debug("Order evicted from cache successfully: {}", orderNumber);
                },
                "evictFromCache",
                orderNumber);
    }

    /**
     * Get cache statistics and health information.
     *
     * @return String containing cache statistics
     */
    public String getCacheStats() {
        return errorHandler.executeWithFallback(
                () -> {
                    StringBuilder stats = new StringBuilder();
                    stats.append("Orders Cache Statistics:\n");
                    stats.append(String.format("  Cache Name: %s\n", ordersCache.getName()));
                    stats.append(String.format("  Cache Size: %d\n", ordersCache.size()));

                    if (ordersCache.getLocalMapStats() != null) {
                        var localStats = ordersCache.getLocalMapStats();
                        stats.append(String.format("  Local Map Stats:\n"));
                        stats.append(String.format("    Owned Entry Count: %d\n", localStats.getOwnedEntryCount()));
                        stats.append(String.format("    Backup Entry Count: %d\n", localStats.getBackupEntryCount()));
                        stats.append(String.format("    Hits: %d\n", localStats.getHits()));
                        stats.append(String.format("    Get Operations: %d\n", localStats.getGetOperationCount()));
                        stats.append(String.format("    Put Operations: %d\n", localStats.getPutOperationCount()));
                    }

                    return stats.toString();
                },
                "getCacheStats",
                "global",
                () -> "Cache stats unavailable due to error\n");
    }

    /**
     * Check cache health by performing a simple operation.
     *
     * @return true if cache is healthy, false otherwise
     */
    public boolean isHealthy() {
        return errorHandler.checkCacheHealth(() -> {
            try {
                // Simple health check - check if we can perform basic operations
                String healthCheckKey = "health-check-" + System.currentTimeMillis();
                ordersCache.put(healthCheckKey, "health-check-value");
                Object value = ordersCache.get(healthCheckKey);
                ordersCache.remove(healthCheckKey);

                return "health-check-value".equals(value);
            } catch (Exception e) {
                logger.debug("Cache health check failed", e);
                return false;
            }
        });
    }

    /**
     * Check if the cache circuit breaker is open (cache unavailable).
     * When the circuit breaker is open, all cache operations will be bypassed
     * and the service will fallback to database operations.
     *
     * @return true if circuit breaker is open (cache unavailable), false otherwise
     */
    public boolean isCircuitBreakerOpen() {
        return errorHandler.isCircuitOpen();
    }

    /**
     * Get circuit breaker status information for monitoring.
     *
     * @return formatted string with circuit breaker status and error statistics
     */
    public String getCircuitBreakerStatus() {
        StringBuilder status = new StringBuilder();
        status.append("Cache Circuit Breaker Status:\n");
        status.append(String.format(
                "  Circuit State: %s\n",
                errorHandler.isCircuitOpen() ? "OPEN (Bypassing Cache)" : "CLOSED (Cache Active)"));
        status.append("  Error Statistics:\n");
        status.append(errorHandler.getCacheErrorStats());

        return status.toString();
    }

    /**
     * Determine if cache operations should fallback to database.
     * This provides a unified decision point for cache vs database operations.
     *
     * @param operationName the name of the operation being considered
     * @return true if should use database, false if cache is available
     */
    public boolean shouldFallbackToDatabase(String operationName) {
        if (errorHandler.isCircuitOpen()) {
            logger.debug("Circuit breaker is open - recommending database fallback for {}", operationName);
            return true;
        }

        return errorHandler.shouldFallbackToDatabase(operationName);
    }

    /**
     * Manually reset the circuit breaker and error state.
     * This can be used for recovery scenarios or testing.
     * Use with caution as it bypasses the automatic recovery mechanism.
     *
     * @return true if reset was successful
     */
    public boolean resetCircuitBreaker() {
        logger.warn("Manually resetting cache circuit breaker - this should only be done for recovery or testing");

        return errorHandler.executeVoidOperation(
                () -> {
                    errorHandler.resetErrorState();
                    logger.info("Cache circuit breaker has been manually reset");
                },
                "resetCircuitBreaker",
                "manual-reset");
    }

    /**
     * Test cache connectivity and attempt to close circuit breaker if open.
     * This method performs a health check and can be used to verify cache recovery.
     *
     * @return true if cache is healthy and available, false otherwise
     */
    public boolean testCacheConnectivity() {
        logger.debug("Testing cache connectivity");

        return errorHandler.checkCacheHealth(() -> {
            try {
                // Comprehensive health check
                String healthCheckKey = "health-check-" + System.currentTimeMillis();
                String healthCheckValue = "connectivity-test";

                // Test basic operations
                ordersCache.put(healthCheckKey, healthCheckValue);
                Object retrieved = ordersCache.get(healthCheckKey);
                boolean exists = ordersCache.containsKey(healthCheckKey);
                ordersCache.remove(healthCheckKey);

                boolean healthy = healthCheckValue.equals(retrieved) && exists;

                if (healthy) {
                    logger.debug("Cache connectivity test passed");
                } else {
                    logger.warn("Cache connectivity test failed - operations didn't work as expected");
                }

                return healthy;

            } catch (Exception e) {
                logger.warn("Cache connectivity test failed with exception: {}", e.getMessage());
                return false;
            }
        });
    }

    /**
     * Get comprehensive cache and circuit breaker health information.
     * This combines cache statistics with circuit breaker status for monitoring.
     *
     * @return formatted health report string
     */
    public String getHealthReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Cache Health Report ===\n");

        // Circuit breaker status
        report.append(getCircuitBreakerStatus());

        // Cache statistics
        report.append("\nCache Statistics:\n");
        report.append(getCacheStats());

        // Connectivity test
        report.append("\nConnectivity Test: ");
        boolean healthy = isHealthy();
        report.append(healthy ? "PASS" : "FAIL");

        if (!healthy && isCircuitBreakerOpen()) {
            report.append("\nRecommendation: Cache is unavailable. Operations will fallback to database.");
        } else if (!healthy) {
            report.append("\nRecommendation: Cache issues detected. Monitor for potential circuit breaker activation.");
        }

        return report.toString();
    }

    /**
     * Find an order with automatic fallback decision based on circuit breaker state.
     * This method demonstrates the circuit breaker pattern in action.
     *
     * @param orderNumber the order number to find
     * @param fallbackFunction function to call if cache is unavailable
     * @return Optional containing the order if found, result of fallback otherwise
     */
    public Optional<OrderEntity> findWithAutomaticFallback(
            String orderNumber, java.util.function.Supplier<Optional<OrderEntity>> fallbackFunction) {
        // Check circuit breaker state first
        if (shouldFallbackToDatabase("findWithAutomaticFallback")) {
            logger.debug("Circuit breaker recommends database fallback for order lookup: {}", orderNumber);
            return fallbackFunction.get();
        }

        // Try cache first
        Optional<OrderEntity> cached = findByOrderNumber(orderNumber);

        // If cache returns empty due to miss (not error), try fallback
        if (cached.isEmpty() && !errorHandler.isCircuitOpen()) {
            logger.debug("Cache miss for order {}, trying fallback", orderNumber);
            return fallbackFunction.get();
        }

        return cached;
    }

    /**
     * Warm up the cache by preloading frequently accessed orders.
     * This method is intended to be called on application startup.
     *
     * @param orderNumbers collection of order numbers to preload
     * @return number of orders successfully preloaded
     */
    public int warmUpCache(Iterable<String> orderNumbers) {
        int successCount = 0;

        logger.info("Starting cache warm-up");

        for (String orderNumber : orderNumbers) {
            boolean warmed = errorHandler.executeWithFallback(
                    () -> {
                        // This will trigger the MapStore to load from database
                        Object value = ordersCache.get(orderNumber);
                        return value != null;
                    },
                    "warmUpCache",
                    orderNumber,
                    () -> false);

            if (warmed) {
                successCount++;
            }
        }

        logger.info("Cache warm-up completed: {} orders preloaded", successCount);
        return successCount;
    }
}
