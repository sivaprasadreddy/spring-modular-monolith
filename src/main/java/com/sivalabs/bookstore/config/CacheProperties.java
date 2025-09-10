package com.sivalabs.bookstore.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for Hazelcast cache settings.
 *
 * This class provides externalized configuration for cache behavior,
 * allowing different settings for different environments.
 */
@ConfigurationProperties(prefix = "bookstore.cache")
@Validated
public class CacheProperties {

    /**
     * Whether cache is enabled. Default is true.
     */
    private boolean enabled = true;

    /**
     * Maximum number of entries in the cache. Default is 1000.
     */
    @Min(1) private int maxSize = 1000;

    /**
     * Time-to-live for cache entries in seconds. Default is 3600 (1 hour).
     */
    @Min(0) private int timeToLiveSeconds = 3600;

    /**
     * Whether write-through mode is enabled. Default is true.
     */
    private boolean writeThrough = true;

    /**
     * Write batch size for batch operations. Default is 1.
     */
    @Min(1) private int writeBatchSize = 1;

    /**
     * Write delay in seconds for write-behind mode. Default is 0 (write-through).
     */
    @Min(0) private int writeDelaySeconds = 0;

    /**
     * Whether cache metrics are enabled. Default is true.
     */
    private boolean metricsEnabled = true;

    /**
     * Whether read backup data is enabled. Default is true.
     */
    private boolean readBackupData = true;

    /**
     * Maximum idle time for cache entries in seconds. Default is 0 (disabled).
     */
    @Min(0) private int maxIdleSeconds = 0;

    /**
     * Number of backup replicas for cache entries. Default is 1.
     */
    @Min(0) private int backupCount = 1;

    public CacheProperties() {}

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getTimeToLiveSeconds() {
        return timeToLiveSeconds;
    }

    public void setTimeToLiveSeconds(int timeToLiveSeconds) {
        this.timeToLiveSeconds = timeToLiveSeconds;
    }

    public boolean isWriteThrough() {
        return writeThrough;
    }

    public void setWriteThrough(boolean writeThrough) {
        this.writeThrough = writeThrough;
    }

    public int getWriteBatchSize() {
        return writeBatchSize;
    }

    public void setWriteBatchSize(int writeBatchSize) {
        this.writeBatchSize = writeBatchSize;
    }

    public int getWriteDelaySeconds() {
        return writeDelaySeconds;
    }

    public void setWriteDelaySeconds(int writeDelaySeconds) {
        this.writeDelaySeconds = writeDelaySeconds;
    }

    public boolean isMetricsEnabled() {
        return metricsEnabled;
    }

    public void setMetricsEnabled(boolean metricsEnabled) {
        this.metricsEnabled = metricsEnabled;
    }

    public boolean isReadBackupData() {
        return readBackupData;
    }

    public void setReadBackupData(boolean readBackupData) {
        this.readBackupData = readBackupData;
    }

    public int getMaxIdleSeconds() {
        return maxIdleSeconds;
    }

    public void setMaxIdleSeconds(int maxIdleSeconds) {
        this.maxIdleSeconds = maxIdleSeconds;
    }

    public int getBackupCount() {
        return backupCount;
    }

    public void setBackupCount(int backupCount) {
        this.backupCount = backupCount;
    }

    @Override
    public String toString() {
        return "CacheProperties{" + "enabled="
                + enabled + ", maxSize="
                + maxSize + ", timeToLiveSeconds="
                + timeToLiveSeconds + ", writeThrough="
                + writeThrough + ", writeBatchSize="
                + writeBatchSize + ", writeDelaySeconds="
                + writeDelaySeconds + ", metricsEnabled="
                + metricsEnabled + ", readBackupData="
                + readBackupData + ", maxIdleSeconds="
                + maxIdleSeconds + ", backupCount="
                + backupCount + '}';
    }
}
