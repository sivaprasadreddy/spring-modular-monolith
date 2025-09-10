# Hazelcast Write-Through 模式實作完整指南

Hazelcast write-through 模式為高效能分散式快取提供了同步資料持久化解決方案。本指南匯整了完整的實作範例、最佳實踐和生產環境部署策略，涵蓋從基本實作到企業級部署的所有面向。

## MapStore 介面實作核心範例

### 基本 MapStore 實作

**PersonMapStore 完整實作**：
```java
public class PersonMapStore implements MapStore<Long, Person> {
    private final Connection con;
    private final PreparedStatement allKeysStatement;

    public PersonMapStore() {
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mydatabase", "user", "password");
            con.createStatement().executeUpdate(
                "create table if not exists person (id bigint not null, name varchar(45), primary key (id))");
            allKeysStatement = con.prepareStatement("select id from person");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void store(Long key, Person value) {
        try {
            con.createStatement().executeUpdate(
                format("insert into person values(%s,'%s')", key, value.getName()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void storeAll(Map<Long, Person> map) {
        for (Map.Entry<Long, Person> entry : map.entrySet()) {
            store(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public synchronized Person load(Long key) {
        try {
            ResultSet resultSet = con.createStatement().executeQuery(
                format("select name from person where id =%s", key));
            try {
                if (!resultSet.next()) {
                    return null;
                }
                String name = resultSet.getString(1);
                return new Person(key, name);
            } finally {
                resultSet.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<Long> loadAllKeys() {
        return new StatementIterable<Long>(allKeysStatement);
    }
}
```

### 生命週期管理實作

**具備資源管理的 MapStore**：
```java
@Component
public class RobustMapStore implements MapStore<String, Object>, MapLoaderLifecycleSupport {
    private HikariDataSource dataSource;
    
    @Override
    public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getProperty("jdbcUrl"));
        config.setUsername(properties.getProperty("username"));
        config.setPassword(properties.getProperty("password"));
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        
        this.dataSource = new HikariDataSource(config);
        System.out.println("MapStore initialized for map: " + mapName);
    }

    @Override
    public void destroy() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
```

## GitHub 開源專案和範例程式碼

### 官方範例專案

**重要的 GitHub 儲存庫**：

1. **Hazelcast MongoDB Write-Through Guide**
   - 儲存庫: `hazelcast-guides/write-through-cache-mongodb-mapstore`
   - 特色: 完整的 MongoDB Atlas 整合與 write-through 快取
   - 最近更新: 2024年
   - 包含: Person 實體、MongoPersonMapStore、儲存庫層

2. **Hazelcast 程式碼範例**
   - 儲存庫: `hazelcast/hazelcast-code-samples`
   - 路徑: `/distributed-map/mapstore/`
   - 特色: 多個 MapStore 範例，包含 LoadAll 功能

3. **MongoDB MapStore 實作**：
```java
public class MongoPersonMapStore implements MapStore<Integer, Person>, MapLoaderLifecycleSupport {
    private MongoClient mongoClient;
    private PersonRepository personRepository;
    
    @Override
    public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
        this.mongoClient = new MongoClient(new MongoClientURI(properties.getProperty("uri")));
        MongoDatabase database = this.mongoClient.getDatabase(properties.getProperty("database"));
        this.personRepository = new MongoPersonRepository(mapName, database);
    }

    @Override
    public void store(Integer key, Person value) {
        getRepository().save(Person.builder()
            .id(key)
            .name(value.getName())
            .lastname(value.getLastname())
            .build());
    }
}
```

## XML 和程式化配置詳細範例

### XML 配置範例

**Write-Through 基本配置**：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<hazelcast xmlns="http://www.hazelcast.com/schema/config">
    <cluster-name>production-cluster</cluster-name>
    
    <!-- Write-Through MapStore 配置 -->
    <map name="user-cache">
        <map-store enabled="true" initial-mode="LAZY">
            <class-name>com.example.UserMapStore</class-name>
            <write-delay-seconds>0</write-delay-seconds> <!-- 0 = write-through -->
            <write-batch-size>1000</write-batch-size>
            <write-coalescing>true</write-coalescing>
            <offload>true</offload>
            <properties>
                <property name="database.url">jdbc:mysql://localhost:3306/test</property>
                <property name="database.username">user</property>
                <property name="database.password">password</property>
            </properties>
        </map-store>
        <backup-count>2</backup-count>
    </map>
</hazelcast>
```

### 程式化配置範例

**完整的 Java 配置**：
```java
@Configuration
public class HazelcastConfig {

    @Bean
    public Config hazelcastConfig(UserMapStore userMapStore) {
        Config config = new Config();
        config.setClusterName("spring-hazelcast-cluster");
        
        // MapStore 配置
        MapConfig userMapConfig = new MapConfig("users");
        MapStoreConfig mapStoreConfig = new MapStoreConfig();
        mapStoreConfig.setImplementation(userMapStore);
        mapStoreConfig.setEnabled(true);
        mapStoreConfig.setWriteDelaySeconds(0); // Write-through 模式
        mapStoreConfig.setWriteBatchSize(100);
        mapStoreConfig.setInitialLoadMode(MapStoreConfig.InitialLoadMode.EAGER);
        
        // 快取逐出設定
        EvictionConfig evictionConfig = new EvictionConfig();
        evictionConfig.setEvictionPolicy(EvictionPolicy.LRU);
        evictionConfig.setSize(1000);
        userMapConfig.setEvictionConfig(evictionConfig);
        
        userMapConfig.setMapStoreConfig(mapStoreConfig);
        userMapConfig.setBackupCount(2);
        config.addMapConfig(userMapConfig);
        
        return config;
    }
}
```

## Spring Boot 整合範例

### 自動配置實作

**Spring Boot 配置類別**：
```java
@SpringBootApplication
@EnableCaching
public class HazelcastSpringBootApplication {

    @Bean
    public Config hazelcastConfig(UserMapStore userMapStore) {
        Config config = new Config();
        config.setInstanceName("spring-boot-hazelcast");
        
        MapConfig userMapConfig = new MapConfig("user-details");
        MapStoreConfig mapStoreConfig = new MapStoreConfig();
        mapStoreConfig.setImplementation(userMapStore);
        mapStoreConfig.setEnabled(true);
        mapStoreConfig.setWriteDelaySeconds(0);
        
        userMapConfig.setMapStoreConfig(mapStoreConfig);
        config.addMapConfig(userMapConfig);
        
        return config;
    }
    
    @Bean
    public IMap<String, User> userMap(HazelcastInstance instance) {
        return instance.getMap("user-details");
    }
}
```

**Spring Boot YAML 配置**：
```yaml
spring:
  hazelcast:
    config: "classpath:config/hazelcast.xml"
  cache:
    type: hazelcast

# application.yml 中的快取配置
hazelcast:
  cluster-name: spring-cluster
  map:
    user-cache:
      map-store:
        enabled: true
        class-name: com.example.UserMapStore
        write-delay-seconds: 0
        write-batch-size: 100
      time-to-live-seconds: 900
      max-idle-seconds: 600
```

## 資料庫整合範例

### MySQL 整合實作

**MySQL MapStore 實作**：
```java
@Component
public class MySQLUserMapStore implements MapStore<Long, User> {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public void store(Long key, User value) {
        try {
            userRepository.save(value);
        } catch (Exception e) {
            logger.error("Failed to store user: " + key, e);
            throw new RuntimeException("Store operation failed", e);
        }
    }

    @Override
    public User load(Long key) {
        return userRepository.findById(key).orElse(null);
    }

    @Override
    public Set<Long> loadAllKeys() {
        return userRepository.findAllIds();
    }
}
```

### PostgreSQL 整合配置

**通用 MapStore 配置**：
```xml
<hazelcast>
    <map name="customer-data">
        <map-store enabled="true">
            <class-name>com.hazelcast.mapstore.GenericMapStore</class-name>
            <properties>
                <property name="data-connection-ref">postgresql-connection</property>
            </properties>
        </map-store>
    </map>
</hazelcast>
```

## 錯誤處理和重試機制

### 自定義重試邏輯

**強固的錯誤處理實作**：
```java
public class RetryableMapStore implements MapStore<String, Object> {
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;
    
    @Override
    public void store(String key, Object value) {
        executeWithRetry(() -> {
            try {
                performDatabaseStore(key, value);
            } catch (SQLException e) {
                if (isTransientError(e)) {
                    throw new RetryableException(e);
                }
                throw new RuntimeException(e);
            }
        });
    }
    
    private void executeWithRetry(Runnable operation) {
        int attempts = 0;
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                operation.run();
                return;
            } catch (RetryableException e) {
                attempts++;
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    throw new RuntimeException("Max retry attempts exceeded", e);
                }
                try {
                    Thread.sleep(RETRY_DELAY_MS * attempts); // 指數退避
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
            }
        }
    }
}
```

### 連線池管理

**HikariCP 連線池配置**：
```java
@Override
public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(properties.getProperty("jdbcUrl"));
    config.setUsername(properties.getProperty("username"));
    config.setPassword(properties.getProperty("password"));
    config.setMaximumPoolSize(20);
    config.setMinimumIdle(5);
    config.setConnectionTimeout(30000);
    config.setIdleTimeout(600000);
    config.setMaxLifetime(1800000);
    config.setLeakDetectionThreshold(60000);
    
    this.dataSource = new HikariDataSource(config);
}
```

## 批次寫入和效能優化

### 批次操作實作

**最佳化批次寫入**：
```java
public class BatchOptimizedMapStore implements MapStore<String, Object> {
    private static final int BATCH_SIZE = 1000;
    
    @Override
    public void storeAll(Map<String, Object> map) {
        Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
        List<Map.Entry<String, Object>> batch = new ArrayList<>();
        
        while (iterator.hasNext()) {
            batch.add(iterator.next());
            
            if (batch.size() >= BATCH_SIZE || !iterator.hasNext()) {
                try {
                    performBatchStore(batch);
                    // 移除成功儲存的項目
                    batch.forEach(entry -> map.remove(entry.getKey()));
                } catch (Exception e) {
                    logger.error("Batch store failed", e);
                    break;
                }
                batch.clear();
            }
        }
    }
    
    private void performBatchStore(List<Map.Entry<String, Object>> batch) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "INSERT INTO table_name (key, value) VALUES (?, ?) ON DUPLICATE KEY UPDATE value = VALUES(value)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Map.Entry<String, Object> entry : batch) {
                    stmt.setString(1, entry.getKey());
                    stmt.setObject(2, entry.getValue());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        }
    }
}
```

### Write-Behind 配置範例

**效能優化配置**：
```xml
<map name="high-throughput-cache">
    <map-store enabled="true" initial-mode="LAZY">
        <class-name>com.example.BatchMapStore</class-name>
        <write-delay-seconds>5</write-delay-seconds>
        <write-batch-size>1000</write-batch-size>
        <write-coalescing>true</write-coalescing>
    </map-store>
</map>
```

## 單元測試和整合測試範例

### JUnit 測試實作

**MapStore 單元測試**：
```java
@RunWith(HazelcastSerialClassRunner.class)
public class MapStoreTest extends JetTestSupport {
    
    @Test
    public void testMapStoreWriteThrough() {
        Config config = new Config();
        MapStoreConfig mapStoreConfig = new MapStoreConfig();
        mapStoreConfig.setEnabled(true)
                      .setImplementation(new TestMapStore());
        
        config.getMapConfig("testMap").setMapStoreConfig(mapStoreConfig);
        
        HazelcastInstance instance = createHazelcastInstance(config);
        IMap<String, String> map = instance.getMap("testMap");
        
        // Write-through 測試
        map.put("key1", "value1");
        
        // 驗證資料已持久化
        assertEquals("value1", map.get("key1"));
        
        Hazelcast.shutdownAll();
    }

    private static class TestMapStore implements MapStore<String, String> {
        private final Map<String, String> store = new ConcurrentHashMap<>();
        
        @Override
        public void store(String key, String value) {
            store.put(key, value);
        }
        
        @Override
        public String load(String key) {
            return store.get(key);
        }
        
        @Override
        public void delete(String key) {
            store.remove(key);
        }
    }
}
```

### Spring Boot 整合測試

**完整整合測試**：
```java
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HazelcastServiceIntegrationTest {
    
    @Autowired
    private HazelcastInstance hazelcastInstance;
    
    @Test
    public void testCacheWriteThrough() {
        IMap<String, String> map = hazelcastInstance.getMap("testCache");
        
        // 測試 write-through 行為
        map.put("testKey", "testValue");
        
        // 驗證資料持久化
        assertThat(map.get("testKey")).isEqualTo("testValue");
    }
    
    @TestConfiguration
    public static class TestConfig {
        @Bean
        @Primary
        public Config hazelcastConfig() {
            Config config = new Config();
            config.setClusterName("test-cluster-" + UUID.randomUUID());
            
            MapStoreConfig mapStoreConfig = new MapStoreConfig();
            mapStoreConfig.setEnabled(true)
                          .setImplementation(new InMemoryMapStore());
            
            config.getMapConfig("*").setMapStoreConfig(mapStoreConfig);
            return config;
        }
    }
}
```

## Docker 和 Kubernetes 部署範例

### Docker Compose 配置

**完整叢集部署**：
```yaml
# docker-compose.yml
version: "3.8"
services:
  hazelcast-1:
    image: hazelcast/hazelcast:5.5.0
    container_name: hazelcast-member-1
    ports:
      - "5701:5701"
    environment:
      - HZ_NETWORK_PUBLICADDRESS=localhost:5701
      - HZ_CLUSTERNAME=production
    volumes:
      - ./hazelcast.yaml:/opt/hazelcast/config/hazelcast.yaml
      - ./custom-libs:/opt/hazelcast/CLASSPATH_EXT
    networks:
      - hazelcast-network

  hazelcast-2:
    image: hazelcast/hazelcast:5.5.0
    container_name: hazelcast-member-2
    ports:
      - "5702:5701"
    environment:
      - HZ_NETWORK_PUBLICADDRESS=localhost:5702
      - HZ_CLUSTERNAME=production
    volumes:
      - ./hazelcast.yaml:/opt/hazelcast/config/hazelcast.yaml
      - ./custom-libs:/opt/hazelcast/CLASSPATH_EXT
    networks:
      - hazelcast-network

  management-center:
    image: hazelcast/management-center:5.8.0
    container_name: hazelcast-mc
    ports:
      - "8080:8080"
    environment:
      - MC_DEFAULT_CLUSTER=production
      - MC_DEFAULT_CLUSTER_MEMBERS=hazelcast-1:5701,hazelcast-2:5701
    networks:
      - hazelcast-network

networks:
  hazelcast-network:
    driver: bridge
```

### Kubernetes 部署配置

**使用 Hazelcast Platform Operator**：
```yaml
apiVersion: hazelcast.com/v1alpha1
kind: Hazelcast
metadata:
  name: production-hazelcast
  namespace: hazelcast-system
spec:
  clusterSize: 3
  clusterName: production-cluster
  
  resources:
    requests:
      memory: "4Gi"
      cpu: "1000m"
    limits:
      memory: "8Gi"
      cpu: "2000m"
  
  # 持久化設定
  persistence:
    enabled: true
    size: 20Gi
    storageClass: fast-ssd
  
  # 自定義配置
  configMapName: hazelcast-production-config
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: hazelcast-production-config
  namespace: hazelcast-system
data:
  hazelcast.yaml: |
    hazelcast:
      cluster-name: production-cluster
      
      network:
        join:
          multicast:
            enabled: false
          kubernetes:
            enabled: true
            service-name: production-hazelcast
            namespace: hazelcast-system
      
      map:
        user-cache:
          map-store:
            enabled: true
            class-name: com.example.UserMapStore
            write-delay-seconds: 0
            write-batch-size: 100
```

### Helm 生產部署配置

**Helm Values 範例**：
```yaml
# values-production.yaml
hazelcast:
  cluster:
    memberCount: 5
    
  resources:
    requests:
      memory: 4Gi
      cpu: 1000m
    limits:
      memory: 8Gi
      cpu: 2000m
      
  javaOpts: >
    -XX:MaxRAMPercentage=80.0
    -XX:+UseG1GC
    -XX:MaxGCPauseMillis=200
    -XX:+PrintGC
    -XX:+PrintGCDetails
    
  persistence:
    enabled: true
    size: 20Gi
    storageClass: ssd-retain
    
  metrics:
    enabled: true
    serviceMonitor:
      enabled: true

managementcenter:
  enabled: true
  resources:
    requests:
      memory: 1Gi
      cpu: 500m
```

## 生產環境最佳實踐

### 生產配置範例

**企業級配置檔**：
```yaml
hazelcast:
  cluster-name: production-cluster
  
  # 網路安全配置
  network:
    ssl:
      enabled: true
      factory-class-name: com.hazelcast.nio.ssl.BasicSSLContextFactory
      properties:
        keyStore: /opt/hazelcast/certs/keystore.jks
        keyStorePassword: ${KEYSTORE_PASSWORD}
        trustStore: /opt/hazelcast/certs/truststore.jks
        protocol: TLSv1.2
    
  # 分割群組以確保高可用性
  partition-group:
    enabled: true
    group-type: ZONE_AWARE
  
  # 關鍵資料的 Write-Through 配置
  map:
    order-cache:
      backup-count: 3
      async-backup-count: 0
      
      map-store:
        enabled: true
        class-name: com.example.OrderMapStore
        write-delay-seconds: 0  # Write-through 模式
        write-batch-size: 1
        
      indexes:
        - type: SORTED
          attributes:
            - "customerId"
        - type: HASH
          attributes:
            - "orderId"
  
  # 監控配置
  metrics:
    enabled: true
    management-center:
      enabled: true
      retention-seconds: 10
    jmx:
      enabled: true
```

### JVM 調優配置

**生產環境 JVM 設定**：
```bash
JAVA_OPTS="
  # 記憶體設定
  -Xms8g -Xmx8g
  -XX:MaxRAMPercentage=80.0
  
  # 垃圾回收
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:G1HeapRegionSize=16m
  -XX:InitiatingHeapOccupancyPercent=45
  
  # GC 日誌
  -Xlog:gc:gc.log:time,uptime,level,tags
  -XX:+UseGCLogFileRotation
  -XX:NumberOfGCLogFiles=5
  -XX:GCLogFileSize=100M
  
  # Hazelcast 特定優化
  -Dhazelcast.operation.thread.count=8
  -Dhazelcast.io.thread.count=4
  -Dhazelcast.partition.count=271
"
```

### 監控和告警配置

**Prometheus 告警規則**：
```yaml
groups:
- name: hazelcast
  rules:
  - alert: HazelcastMemberDown
    expr: hz_cluster_size < 3
    for: 2m
    labels:
      severity: critical
    annotations:
      summary: "Hazelcast 叢集成員不足"
      description: "叢集 {{ $labels.cluster }} 只有 {{ $value }} 個成員"

  - alert: HazelcastHighMemoryUsage
    expr: (hz_memory_usedHeap / hz_memory_maxHeap) * 100 > 85
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "Hazelcast 記憶體使用率過高"
      description: "成員 {{ $labels.instance }} 使用了 {{ $value }}% 堆積記憶體"
```

### 健康檢查腳本

**生產環境健康監控**：
```bash
#!/bin/bash
# health-check.sh
HAZELCAST_HOST="localhost"
HAZELCAST_PORT="5701"
HEALTH_CHECK_URL="http://${HAZELCAST_HOST}:${HAZELCAST_PORT}/hazelcast/health"

check_cluster_health() {
    response=$(curl -s "${HEALTH_CHECK_URL}")
    cluster_safe=$(echo "$response" | jq -r '.clusterSafe')
    cluster_size=$(echo "$response" | jq -r '.clusterSize')
    
    if [[ "$cluster_safe" == "true" && "$cluster_size" -ge 3 ]]; then
        echo "健康: 叢集安全，有 $cluster_size 個成員"
        exit 0
    else
        echo "不健康: 叢集安全性: $cluster_safe, 大小: $cluster_size"
        exit 1
    fi
}

check_cluster_health
```

## 結論

本指南提供了 Hazelcast write-through 模式的完整實作範例，從基本的 MapStore 實作到企業級生產部署。**重要的設定原則**包括：write-through 模式設定 `write-delay-seconds` 為 0，適當的連線池管理，強固的錯誤處理機制，以及完整的監控和告警策略。

這些實作範例和最佳實踐為開發團隊提供了從開發測試到生產部署的完整解決方案，確保資料一致性和系統高可用性。透過適當的配置調優和監控，Hazelcast write-through 模式能夠為分散式應用提供可靠的快取持久化解決方案。