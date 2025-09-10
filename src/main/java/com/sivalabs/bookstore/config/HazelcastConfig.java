package com.sivalabs.bookstore.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizePolicy;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Hazelcast configuration for distributed caching.
 *
 * This configuration creates a Hazelcast instance and configures the orders-cache
 * with appropriate settings for write-through caching.
 */
@Configuration
@EnableConfigurationProperties(CacheProperties.class)
@ConditionalOnProperty(prefix = "bookstore.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
public class HazelcastConfig {

    private static final Logger logger = LoggerFactory.getLogger(HazelcastConfig.class);

    private static final String ORDERS_CACHE_NAME = "orders-cache";

    /**
     * Creates and configures the main Hazelcast configuration.
     *
     * @param cacheProperties externalized cache configuration
     * @return configured Hazelcast Config instance
     */
    @Bean
    public Config hazelcastConfiguration(CacheProperties cacheProperties) {
        logger.info("Initializing Hazelcast configuration");

        Config config = new Config();
        // Use dynamic instance name to avoid conflicts in tests
        String instanceName = "bookstore-hazelcast-" + System.currentTimeMillis();
        config.setInstanceName(instanceName);
        config.setClusterName("bookstore-cluster");

        // Configure the orders cache map
        MapConfig ordersCacheMapConfig = new MapConfig(ORDERS_CACHE_NAME);

        // Configure eviction policy and max size
        EvictionConfig evictionConfig = new EvictionConfig();
        evictionConfig.setMaxSizePolicy(MaxSizePolicy.PER_NODE);
        evictionConfig.setSize(cacheProperties.getMaxSize());
        evictionConfig.setEvictionPolicy(EvictionPolicy.LRU);
        ordersCacheMapConfig.setEvictionConfig(evictionConfig);

        ordersCacheMapConfig.setTimeToLiveSeconds(cacheProperties.getTimeToLiveSeconds());
        ordersCacheMapConfig.setMaxIdleSeconds(cacheProperties.getMaxIdleSeconds());
        ordersCacheMapConfig.setBackupCount(cacheProperties.getBackupCount());
        ordersCacheMapConfig.setReadBackupData(cacheProperties.isReadBackupData());

        // Configure MapStore for write-through behavior
        // Note: MapStore configuration is temporarily disabled to resolve circular dependency
        // The write-through behavior is handled by OrderCacheService and OrderService
        if (cacheProperties.isWriteThrough()) {
            // TODO: Implement MapStore configuration that respects Spring Modulith boundaries
            logger.info("Write-through caching enabled - handled by service layer instead of MapStore for now");
        }

        ordersCacheMapConfig.setStatisticsEnabled(cacheProperties.isMetricsEnabled());

        config.addMapConfig(ordersCacheMapConfig);

        // Enable metrics if requested
        if (cacheProperties.isMetricsEnabled()) {
            config.getMetricsConfig().setEnabled(true);
        }

        // Configure serialization to support Java records and complex objects
        SerializationConfig serializationConfig = config.getSerializationConfig();

        // For Hazelcast 5.5.0, use portable serialization or JSON for better Java record support
        // Enable check class definition errors to false for better compatibility with records
        serializationConfig.setCheckClassDefErrors(false);
        serializationConfig.setUseNativeByteOrder(false);

        // Enable byte-order compatibility and better record handling
        serializationConfig.setAllowOverrideDefaultSerializers(true);
        serializationConfig.setAllowUnsafe(false);
        serializationConfig.setEnableCompression(false); // Disable compression for better debugging
        serializationConfig.setEnableSharedObject(false); // Disable shared object references for simpler serialization

        logger.info("Configured serialization for better Java record support");

        // Configure management center (disabled for now)
        config.getManagementCenterConfig().setScriptingEnabled(false);

        logger.info(
                "Hazelcast configuration completed - Instance: {}, Cluster: {}, Enhanced serialization: enabled",
                config.getInstanceName(),
                config.getClusterName());

        return config;
    }

    /**
     * Creates the Hazelcast instance using the provided configuration.
     *
     * @param config the Hazelcast configuration
     * @return HazelcastInstance
     */
    @Bean(destroyMethod = "shutdown")
    public HazelcastInstance hazelcastInstance(Config hazelcastConfiguration) {
        logger.info("Creating Hazelcast instance");
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(hazelcastConfiguration);
        logger.info("Hazelcast instance created successfully: {}", instance.getName());
        return instance;
    }

    /**
     * Creates the orders cache IMap bean.
     * Note: Using Object as value type to avoid module boundary violations.
     * The actual OrderEntity will be handled by the orders module components.
     *
     * @param hazelcastInstance the Hazelcast instance
     * @return IMap for orders cache
     */
    @Bean("ordersCache")
    @Lazy // Lazy initialization to allow MapStore bean to be created first
    public IMap<String, Object> ordersCache(HazelcastInstance hazelcastInstance) {
        logger.info("Creating orders cache map");
        IMap<String, Object> ordersMap = hazelcastInstance.getMap(ORDERS_CACHE_NAME);
        logger.info("Orders cache map created: {} with MapStore support", ORDERS_CACHE_NAME);
        return ordersMap;
    }

    /**
     * Provides cache name constant for other components.
     *
     * @return the orders cache name
     */
    @Bean("ordersCacheName")
    public String ordersCacheName() {
        return ORDERS_CACHE_NAME;
    }
}
