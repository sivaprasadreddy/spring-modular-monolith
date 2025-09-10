package com.sivalabs.bookstore.config;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Spring Boot Actuator health indicator for Hazelcast cache.
 * This provides health monitoring and diagnostics for the cache infrastructure.
 */
@Component
@ConditionalOnProperty(prefix = "bookstore.cache", name = "enabled", havingValue = "true")
public class CacheHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(CacheHealthIndicator.class);
    private static final String HEALTH_CHECK_KEY = "health-check-indicator";

    private final HazelcastInstance hazelcastInstance;
    private final IMap<String, Object> ordersCache;
    private final CacheProperties cacheProperties;

    public CacheHealthIndicator(
            HazelcastInstance hazelcastInstance,
            @Qualifier("ordersCache") IMap<String, Object> ordersCache,
            CacheProperties cacheProperties) {
        this.hazelcastInstance = hazelcastInstance;
        this.ordersCache = ordersCache;
        this.cacheProperties = cacheProperties;
        logger.info("CacheHealthIndicator initialized");
    }

    @Override
    public Health health() {
        try {
            return checkCacheHealth();
        } catch (Exception e) {
            logger.error("Cache health check failed with exception", e);
            return Health.down()
                    .withDetail("status", "DOWN")
                    .withDetail("error", e.getMessage())
                    .withDetail("exception", e.getClass().getSimpleName())
                    .withDetail("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();
        }
    }

    private Health checkCacheHealth() {
        Map<String, Object> details = new HashMap<>();
        boolean healthy = true;

        // Check Hazelcast instance
        try {
            if (hazelcastInstance == null) {
                healthy = false;
                details.put("hazelcast", "NULL_INSTANCE");
            } else {
                details.put("hazelcast", "CONNECTED");
                details.put("instanceName", hazelcastInstance.getName());
                details.put("clusterName", hazelcastInstance.getConfig().getClusterName());
                details.put(
                        "memberCount",
                        hazelcastInstance.getCluster().getMembers().size());
                details.put(
                        "instanceRunning",
                        hazelcastInstance.getLifecycleService().isRunning());

                if (!hazelcastInstance.getLifecycleService().isRunning()) {
                    healthy = false;
                }
            }
        } catch (Exception e) {
            healthy = false;
            details.put("hazelcast", "ERROR: " + e.getMessage());
            logger.warn("Hazelcast instance check failed", e);
        }

        // Check orders cache availability
        try {
            if (ordersCache != null) {
                details.put("ordersCache", "AVAILABLE");
                details.put("cacheName", ordersCache.getName());
                details.put("cacheSize", ordersCache.size());

                // Get local map statistics if available
                if (ordersCache.getLocalMapStats() != null) {
                    var stats = ordersCache.getLocalMapStats();
                    details.put(
                            "localStats",
                            Map.of(
                                    "ownedEntries", stats.getOwnedEntryCount(),
                                    "backupEntries", stats.getBackupEntryCount(),
                                    "hits", stats.getHits(),
                                    "getOperations", stats.getGetOperationCount(),
                                    "putOperations", stats.getPutOperationCount()));
                }
            } else {
                details.put("ordersCache", "NULL_CACHE");
                healthy = false;
            }
        } catch (Exception e) {
            healthy = false;
            details.put("ordersCache", "ERROR: " + e.getMessage());
            logger.warn("Orders cache check failed", e);
        }

        // Test basic cache operations if cache is available
        if (hazelcastInstance != null && ordersCache != null && healthy) {
            try {
                testBasicCacheOperations(details);
            } catch (Exception e) {
                healthy = false;
                details.put("basicOperations", "ERROR: " + e.getMessage());
                logger.warn("Basic cache operations test failed", e);
            }
        }

        // Add configuration details
        details.put(
                "config",
                Map.of(
                        "enabled", cacheProperties.isEnabled(),
                        "writeThrough", cacheProperties.isWriteThrough(),
                        "maxSize", cacheProperties.getMaxSize(),
                        "timeToLive", cacheProperties.getTimeToLiveSeconds()));

        // Add timestamp
        details.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        if (healthy) {
            return Health.up().withDetails(details).build();
        } else {
            return Health.down().withDetails(details).build();
        }
    }

    private void testBasicCacheOperations(Map<String, Object> details) {
        try {
            IMap<String, String> testMap = hazelcastInstance.getMap("health-check-map");

            String testKey = HEALTH_CHECK_KEY + "-" + System.currentTimeMillis();
            String testValue = "health-test-" + System.currentTimeMillis();

            // Test put operation
            testMap.put(testKey, testValue);

            // Test get operation
            String retrievedValue = testMap.get(testKey);
            boolean getSuccess = testValue.equals(retrievedValue);

            // Test containsKey operation
            boolean containsKey = testMap.containsKey(testKey);

            // Test remove operation
            String removedValue = testMap.remove(testKey);
            boolean removeSuccess = testValue.equals(removedValue);

            // Test final state
            boolean finalCleanup = !testMap.containsKey(testKey);

            boolean allOperationsSuccess = getSuccess && containsKey && removeSuccess && finalCleanup;

            details.put("basicOperations", allOperationsSuccess ? "OK" : "PARTIAL_FAILURE");
            details.put(
                    "operationsDetail",
                    Map.of(
                            "put", "OK",
                            "get", getSuccess ? "OK" : "FAILED",
                            "containsKey", containsKey ? "OK" : "FAILED",
                            "remove", removeSuccess ? "OK" : "FAILED",
                            "cleanup", finalCleanup ? "OK" : "FAILED"));

            if (!allOperationsSuccess) {
                logger.warn(
                        "Some basic cache operations failed: get={}, containsKey={}, remove={}, cleanup={}",
                        getSuccess,
                        containsKey,
                        removeSuccess,
                        finalCleanup);
            }

        } catch (Exception e) {
            details.put("basicOperations", "EXCEPTION");
            details.put("operationsError", e.getMessage());
            throw e; // Re-throw to be handled by caller
        }
    }

    /**
     * Get detailed health information for debugging.
     * This method can be used for more comprehensive health reporting.
     *
     * @return Health information with extended details
     */
    public Map<String, Object> getDetailedHealthInfo() {
        Map<String, Object> healthInfo = new HashMap<>();

        try {
            Health health = health();
            healthInfo.put("status", health.getStatus().getCode());
            healthInfo.putAll(health.getDetails());

            // Add additional diagnostic information about Hazelcast
            if (hazelcastInstance != null) {
                try {
                    healthInfo.put(
                            "clusterState", hazelcastInstance.getCluster().getClusterState());
                    healthInfo.put(
                            "partitionServiceSafe",
                            hazelcastInstance.getPartitionService().isClusterSafe());
                    healthInfo.put(
                            "lifecycleRunning",
                            hazelcastInstance.getLifecycleService().isRunning());
                } catch (Exception e) {
                    healthInfo.put("diagnosticsError", e.getMessage());
                }
            }

            // Add orders cache detailed statistics
            if (ordersCache != null) {
                try {
                    healthInfo.put(
                            "ordersCacheDetails",
                            Map.of(
                                    "name", ordersCache.getName(),
                                    "size", ordersCache.size(),
                                    "isEmpty", ordersCache.isEmpty(),
                                    "statisticsEnabled", ordersCache.getLocalMapStats() != null));
                } catch (Exception e) {
                    healthInfo.put("ordersCacheError", e.getMessage());
                }
            }

        } catch (Exception e) {
            healthInfo.put("healthCheckError", e.getMessage());
            healthInfo.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        return healthInfo;
    }
}
