package com.sivalabs.bookstore.config;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for cache-related metrics using Micrometer.
 * This provides monitoring for cache hit/miss rates, size, and performance metrics.
 */
@Configuration
@ConditionalOnProperty(prefix = "bookstore.cache", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "bookstore.cache", name = "metrics-enabled", havingValue = "true")
public class CacheMetricsConfig {

    private static final Logger logger = LoggerFactory.getLogger(CacheMetricsConfig.class);
    private static final String CACHE_METRICS_PREFIX = "bookstore.cache";

    private final MeterRegistry meterRegistry;
    private final HazelcastInstance hazelcastInstance;
    private final IMap<String, Object> ordersCache;
    private final CacheProperties cacheProperties;

    // Custom metrics counters
    private Counter cacheHitCounter;
    private Counter cacheMissCounter;
    private Counter cacheEvictionCounter;
    private Counter cacheErrorCounter;
    private Timer cacheOperationTimer;

    // Circuit breaker metrics
    private final AtomicLong circuitBreakerOpenTime = new AtomicLong(0);
    private final AtomicLong circuitBreakerTotalFailures = new AtomicLong(0);

    public CacheMetricsConfig(
            MeterRegistry meterRegistry,
            HazelcastInstance hazelcastInstance,
            @Qualifier("ordersCache") IMap<String, Object> ordersCache,
            CacheProperties cacheProperties) {
        this.meterRegistry = meterRegistry;
        this.hazelcastInstance = hazelcastInstance;
        this.ordersCache = ordersCache;
        this.cacheProperties = cacheProperties;
    }

    @PostConstruct
    public void initializeCacheMetrics() {
        logger.info("Initializing cache metrics for Prometheus monitoring");

        // Initialize custom counters
        cacheHitCounter = Counter.builder(CACHE_METRICS_PREFIX + ".hits")
                .description("Number of cache hits")
                .tag("cache", "orders-cache")
                .register(meterRegistry);

        cacheMissCounter = Counter.builder(CACHE_METRICS_PREFIX + ".misses")
                .description("Number of cache misses")
                .tag("cache", "orders-cache")
                .register(meterRegistry);

        cacheEvictionCounter = Counter.builder(CACHE_METRICS_PREFIX + ".evictions")
                .description("Number of cache evictions")
                .tag("cache", "orders-cache")
                .register(meterRegistry);

        cacheErrorCounter = Counter.builder(CACHE_METRICS_PREFIX + ".errors")
                .description("Number of cache operation errors")
                .tag("cache", "orders-cache")
                .register(meterRegistry);

        cacheOperationTimer = Timer.builder(CACHE_METRICS_PREFIX + ".operation.duration")
                .description("Time taken for cache operations")
                .tag("cache", "orders-cache")
                .register(meterRegistry);

        // Register cache size gauge
        Gauge.builder(CACHE_METRICS_PREFIX + ".size", this, CacheMetricsConfig::getCacheSize)
                .description("Current number of entries in cache")
                .tag("cache", "orders-cache")
                .register(meterRegistry);

        // Register cache hit ratio gauge
        Gauge.builder(CACHE_METRICS_PREFIX + ".hit.ratio", this, CacheMetricsConfig::getCacheHitRatio)
                .description("Cache hit ratio (hits / (hits + misses))")
                .tag("cache", "orders-cache")
                .register(meterRegistry);

        // Register Hazelcast cluster metrics
        registerHazelcastMetrics();

        // Register configuration metrics
        registerConfigurationMetrics();

        // Register circuit breaker metrics
        registerCircuitBreakerMetrics();

        logger.info("Cache metrics initialization completed");
    }

    private void registerHazelcastMetrics() {
        // Cluster member count
        Gauge.builder(CACHE_METRICS_PREFIX + ".cluster.members", hazelcastInstance, hz -> hz.getCluster()
                        .getMembers()
                        .size())
                .description("Number of cluster members")
                .register(meterRegistry);

        // Instance running status
        Gauge.builder(
                        CACHE_METRICS_PREFIX + ".instance.running",
                        hazelcastInstance,
                        hz -> hz.getLifecycleService().isRunning() ? 1 : 0)
                .description("Hazelcast instance running status (1=running, 0=stopped)")
                .register(meterRegistry);

        // Partition safety
        Gauge.builder(
                        CACHE_METRICS_PREFIX + ".cluster.safe",
                        hazelcastInstance,
                        hz -> hz.getPartitionService().isClusterSafe() ? 1 : 0)
                .description("Cluster partition safety status (1=safe, 0=unsafe)")
                .register(meterRegistry);
    }

    private void registerConfigurationMetrics() {
        // Cache configuration as gauges for monitoring
        Gauge.builder(CACHE_METRICS_PREFIX + ".config.enabled", cacheProperties, props -> props.isEnabled() ? 1 : 0)
                .description("Cache enabled status (1=enabled, 0=disabled)")
                .register(meterRegistry);

        Gauge.builder(CACHE_METRICS_PREFIX + ".config.max_size", cacheProperties, CacheProperties::getMaxSize)
                .description("Configured maximum cache size")
                .register(meterRegistry);

        Gauge.builder(
                        CACHE_METRICS_PREFIX + ".config.ttl_seconds",
                        cacheProperties,
                        CacheProperties::getTimeToLiveSeconds)
                .description("Configured time-to-live in seconds")
                .register(meterRegistry);

        Gauge.builder(
                        CACHE_METRICS_PREFIX + ".config.write_through",
                        cacheProperties,
                        props -> props.isWriteThrough() ? 1 : 0)
                .description("Write-through configuration (1=enabled, 0=disabled)")
                .register(meterRegistry);
    }

    private void registerCircuitBreakerMetrics() {
        // Circuit breaker state
        Gauge.builder(CACHE_METRICS_PREFIX + ".circuit_breaker.open_time", circuitBreakerOpenTime, AtomicLong::get)
                .description("Time when circuit breaker was opened (timestamp)")
                .register(meterRegistry);

        Gauge.builder(
                        CACHE_METRICS_PREFIX + ".circuit_breaker.total_failures",
                        circuitBreakerTotalFailures,
                        AtomicLong::get)
                .description("Total number of circuit breaker failures")
                .register(meterRegistry);
    }

    // Helper methods for metric calculations
    private double getCacheSize() {
        try {
            return ordersCache.size();
        } catch (Exception e) {
            logger.warn("Failed to get cache size for metrics: {}", e.getMessage());
            return 0;
        }
    }

    private double getCacheHitRatio() {
        try {
            var stats = ordersCache.getLocalMapStats();
            if (stats != null) {
                long hits = stats.getHits();
                long gets = stats.getGetOperationCount();
                return gets > 0 ? (double) hits / gets : 0.0;
            }
        } catch (Exception e) {
            logger.warn("Failed to calculate cache hit ratio for metrics: {}", e.getMessage());
        }
        return 0.0;
    }

    // Public methods for recording custom metrics

    /**
     * Record a cache hit event.
     */
    public void recordCacheHit() {
        cacheHitCounter.increment();
    }

    /**
     * Record a cache miss event.
     */
    public void recordCacheMiss() {
        cacheMissCounter.increment();
    }

    /**
     * Record a cache eviction event.
     */
    public void recordCacheEviction() {
        cacheEvictionCounter.increment();
    }

    /**
     * Record a cache operation error.
     */
    public void recordCacheError() {
        cacheErrorCounter.increment();
    }

    /**
     * Record a cache operation duration.
     * @param operation The operation name for tagging
     * @return Timer.Sample to stop when operation completes
     */
    public Timer.Sample startCacheOperationTimer(String operation) {
        return Timer.start(meterRegistry);
    }

    /**
     * Stop the cache operation timer and record the duration.
     * @param sample The timer sample from startCacheOperationTimer
     */
    public void stopCacheOperationTimer(Timer.Sample sample) {
        sample.stop(cacheOperationTimer);
    }

    /**
     * Record circuit breaker opened event.
     */
    public void recordCircuitBreakerOpened() {
        circuitBreakerOpenTime.set(System.currentTimeMillis());
        circuitBreakerTotalFailures.incrementAndGet();
    }

    /**
     * Record circuit breaker closed event.
     */
    public void recordCircuitBreakerClosed() {
        circuitBreakerOpenTime.set(0);
    }

    /**
     * Get current metrics summary for logging/debugging.
     * @return formatted metrics summary
     */
    public String getMetricsSummary() {
        try {
            var stats = ordersCache.getLocalMapStats();
            if (stats != null) {
                return String.format(
                        "Cache Metrics Summary - Size: %d, Hits: %d, Gets: %d, Hit Ratio: %.2f%%, "
                                + "Puts: %d, Removes: %d, Evictions: %d",
                        ordersCache.size(),
                        stats.getHits(),
                        stats.getGetOperationCount(),
                        getCacheHitRatio() * 100,
                        stats.getPutOperationCount(),
                        stats.getRemoveOperationCount(),
                        stats.getNearCacheStats() != null
                                ? stats.getNearCacheStats().getEvictions()
                                : 0);
            }
        } catch (Exception e) {
            logger.warn("Failed to generate metrics summary: {}", e.getMessage());
        }
        return "Cache metrics unavailable";
    }

    /**
     * Check if metrics are properly configured and working.
     * @return true if metrics are functional
     */
    public boolean isMetricsHealthy() {
        try {
            // Test basic metrics functionality
            double size = getCacheSize();
            double hitRatio = getCacheHitRatio();
            boolean hazelcastRunning = hazelcastInstance.getLifecycleService().isRunning();

            return size >= 0 && hitRatio >= 0 && hazelcastRunning;
        } catch (Exception e) {
            logger.warn("Metrics health check failed: {}", e.getMessage());
            return false;
        }
    }
}
