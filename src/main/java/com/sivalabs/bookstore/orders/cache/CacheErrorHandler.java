package com.sivalabs.bookstore.orders.cache;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Centralized error handler for cache operations.
 *
 * This utility provides consistent error handling, logging, and fallback decision-making
 * for all cache-related operations. It implements a basic circuit breaker pattern to
 * prevent cascading failures when the cache becomes unavailable.
 *
 * Key features:
 * - Centralized error logging with contextual information
 * - Circuit breaker pattern for cache availability
 * - Fallback decision support
 * - Error recovery tracking
 * - Performance degradation monitoring
 */
@Component
public class CacheErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(CacheErrorHandler.class);

    // Circuit breaker configuration
    private static final int FAILURE_THRESHOLD = 5;
    private static final Duration CIRCUIT_OPEN_DURATION = Duration.ofMinutes(2);

    // Error tracking
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private volatile LocalDateTime circuitOpenedAt = null;
    private volatile boolean circuitOpen = false;

    // Error metrics tracking
    private final ConcurrentHashMap<String, AtomicInteger> errorCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> lastErrorTimes = new ConcurrentHashMap<>();

    /**
     * Execute a cache operation with error handling.
     * If the operation fails, it will be logged and null will be returned.
     *
     * @param operation the cache operation to execute
     * @param operationName descriptive name of the operation for logging
     * @param key the cache key involved in the operation
     * @param <T> the return type of the operation
     * @return the result of the operation, or null if it fails
     */
    public <T> T executeWithFallback(Supplier<T> operation, String operationName, String key) {
        return executeWithFallback(operation, operationName, key, () -> null);
    }

    /**
     * Execute a cache operation with error handling and a custom fallback.
     *
     * @param operation the cache operation to execute
     * @param operationName descriptive name of the operation for logging
     * @param key the cache key involved in the operation
     * @param fallback the fallback operation to execute if the primary operation fails
     * @param <T> the return type of the operation
     * @return the result of the operation, or the fallback result if it fails
     */
    public <T> T executeWithFallback(Supplier<T> operation, String operationName, String key, Supplier<T> fallback) {
        // Check circuit breaker state
        if (isCircuitOpen()) {
            logger.debug("Circuit breaker is open, skipping cache operation: {} for key: {}", operationName, key);
            recordError(operationName, "Circuit breaker open");
            return fallback.get();
        }

        try {
            T result = operation.get();
            recordSuccess(operationName);
            return result;

        } catch (Exception e) {
            handleCacheError(e, operationName, key);
            return fallback.get();
        }
    }

    /**
     * Execute a cache operation without a return value (void operations).
     *
     * @param operation the cache operation to execute
     * @param operationName descriptive name of the operation for logging
     * @param key the cache key involved in the operation
     * @return true if the operation succeeded, false if it failed
     */
    public boolean executeVoidOperation(Runnable operation, String operationName, String key) {
        // Check circuit breaker state
        if (isCircuitOpen()) {
            logger.debug("Circuit breaker is open, skipping cache operation: {} for key: {}", operationName, key);
            recordError(operationName, "Circuit breaker open");
            return false;
        }

        try {
            operation.run();
            recordSuccess(operationName);
            return true;

        } catch (Exception e) {
            handleCacheError(e, operationName, key);
            return false;
        }
    }

    /**
     * Handle cache errors consistently across all operations.
     *
     * @param exception the exception that occurred
     * @param operationName the name of the operation that failed
     * @param key the cache key involved
     */
    public void handleCacheError(Exception exception, String operationName, String key) {
        recordError(operationName, exception.getMessage());

        // Log the error with context
        logger.warn(
                "Cache operation failed - Operation: {}, Key: {}, Error: {}",
                operationName,
                key,
                exception.getMessage());

        // Log the full stack trace at debug level for troubleshooting
        logger.debug("Cache operation failure details for {} with key {}", operationName, key, exception);

        // Update circuit breaker state
        int failures = consecutiveFailures.incrementAndGet();
        if (failures >= FAILURE_THRESHOLD && !circuitOpen) {
            openCircuit();
        }
    }

    /**
     * Check if cache operations should be allowed or if we should fallback.
     *
     * @return true if the circuit breaker is open (cache unavailable), false otherwise
     */
    public boolean isCircuitOpen() {
        if (!circuitOpen) {
            return false;
        }

        // Check if circuit should be closed (recovery attempt)
        LocalDateTime openedAt = circuitOpenedAt;
        if (openedAt != null && Duration.between(openedAt, LocalDateTime.now()).compareTo(CIRCUIT_OPEN_DURATION) > 0) {
            // Time to try again - half-open state
            logger.info("Circuit breaker entering half-open state - attempting cache recovery");
            return false;
        }

        return true;
    }

    /**
     * Manually check cache health and potentially close the circuit breaker.
     * This can be called periodically or after successful database operations
     * to test if the cache has recovered.
     *
     * @param healthCheck a simple operation to test cache connectivity
     * @return true if the cache appears to be healthy, false otherwise
     */
    public boolean checkCacheHealth(Supplier<Boolean> healthCheck) {
        try {
            if (healthCheck.get()) {
                closeCircuit();
                logger.info("Cache health check passed - circuit breaker closed");
                return true;
            } else {
                logger.debug("Cache health check failed - circuit breaker remains open");
                return false;
            }
        } catch (Exception e) {
            logger.warn("Cache health check threw exception: {}", e.getMessage());
            recordError("health-check", e.getMessage());
            return false;
        }
    }

    /**
     * Get current cache error statistics for monitoring.
     *
     * @return formatted string with error statistics
     */
    public String getCacheErrorStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("Cache Error Statistics:\n");
        stats.append(String.format("  Circuit Breaker: %s\n", circuitOpen ? "OPEN" : "CLOSED"));
        stats.append(String.format("  Consecutive Failures: %d\n", consecutiveFailures.get()));

        if (circuitOpenedAt != null) {
            stats.append(String.format("  Circuit Opened At: %s\n", circuitOpenedAt));
        }

        errorCounts.forEach((operation, count) -> stats.append(
                String.format("  %s Errors: %d (Last: %s)\n", operation, count.get(), lastErrorTimes.get(operation))));

        return stats.toString();
    }

    /**
     * Reset error tracking and close circuit breaker.
     * Useful for testing or manual recovery.
     */
    public void resetErrorState() {
        consecutiveFailures.set(0);
        circuitOpen = false;
        circuitOpenedAt = null;
        errorCounts.clear();
        lastErrorTimes.clear();
        logger.info("Cache error state has been reset");
    }

    /**
     * Determine if fallback to database is recommended based on error patterns.
     *
     * @param operationName the name of the operation
     * @return true if database fallback is recommended
     */
    public boolean shouldFallbackToDatabase(String operationName) {
        if (isCircuitOpen()) {
            return true;
        }

        AtomicInteger operationErrors = errorCounts.get(operationName);
        if (operationErrors != null && operationErrors.get() > 3) {
            logger.debug("Recommending database fallback for {} due to repeated errors", operationName);
            return true;
        }

        return false;
    }

    private void recordSuccess(String operationName) {
        // Reset consecutive failures on success
        consecutiveFailures.set(0);

        // If circuit was open, close it on first success
        if (circuitOpen) {
            closeCircuit();
            logger.debug("Cache operation {} succeeded after circuit breaker recovery", operationName);
        } else {
            logger.trace("Cache operation {} completed successfully", operationName);
        }
    }

    private void recordError(String operationName, String errorMessage) {
        errorCounts.computeIfAbsent(operationName, k -> new AtomicInteger(0)).incrementAndGet();
        lastErrorTimes.put(operationName, LocalDateTime.now());

        logger.debug("Recorded error for operation {}: {}", operationName, errorMessage);
    }

    private void openCircuit() {
        circuitOpen = true;
        circuitOpenedAt = LocalDateTime.now();
        logger.warn(
                "Circuit breaker OPENED - Cache operations will be bypassed for {} seconds",
                CIRCUIT_OPEN_DURATION.getSeconds());
    }

    private void closeCircuit() {
        circuitOpen = false;
        circuitOpenedAt = null;
        consecutiveFailures.set(0);
        logger.info("Circuit breaker CLOSED - Cache operations resumed");
    }
}
