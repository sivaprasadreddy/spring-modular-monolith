package com.sivalabs.bookstore.orders.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.hazelcast.core.HazelcastInstance;
import com.sivalabs.bookstore.orders.domain.OrderEntity;
import com.sivalabs.bookstore.orders.domain.OrderRepository;
import com.sivalabs.bookstore.orders.domain.models.Customer;
import com.sivalabs.bookstore.orders.domain.models.OrderItem;
import com.sivalabs.bookstore.orders.domain.models.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderMapStore Unit Tests")
class OrderMapStoreTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private HazelcastInstance hazelcastInstance;

    private OrderMapStore orderMapStore;

    private OrderEntity testOrder;
    private OrderEntity anotherTestOrder;

    @BeforeEach
    void setUp() {
        orderMapStore = new OrderMapStore(orderRepository);

        // Create test order data
        testOrder = createTestOrder("ORD-001", 1L, "Test Product", 2);
        anotherTestOrder = createTestOrder("ORD-002", 2L, "Another Product", 1);
    }

    @Test
    @DisplayName("Should initialize successfully with OrderRepository")
    void shouldInitializeWithOrderRepository() {
        assertThat(orderMapStore).isNotNull();
    }

    @Test
    @DisplayName("Should complete lifecycle init without errors")
    void shouldInitializeLifecycle() {
        Properties props = new Properties();
        String mapName = "test-map";

        // Should not throw any exception
        orderMapStore.init(hazelcastInstance, props, mapName);
    }

    @Test
    @DisplayName("Should complete lifecycle destroy without errors")
    void shouldDestroyLifecycle() {
        // Should not throw any exception
        orderMapStore.destroy();
    }

    @Test
    @DisplayName("Should store order successfully - validation only")
    void shouldStoreOrderSuccessfully() {
        String orderNumber = "ORD-001";

        // Store operation should complete without exception
        orderMapStore.store(orderNumber, testOrder);

        // Verify no exception was thrown - store method mainly validates
    }

    @Test
    @DisplayName("Should handle store with mismatched order number")
    void shouldHandleStoreWithMismatchedOrderNumber() {
        String differentOrderNumber = "ORD-DIFFERENT";

        // Should complete without throwing exception but log warning
        orderMapStore.store(differentOrderNumber, testOrder);
    }

    @Test
    @DisplayName("Should handle store with null order entity")
    void shouldHandleStoreWithNullOrder() {
        String orderNumber = "ORD-001";

        // Should complete without throwing exception
        orderMapStore.store(orderNumber, null);
    }

    @Test
    @DisplayName("Should load order successfully from OrderRepository")
    void shouldLoadOrderSuccessfully() {
        String orderNumber = "ORD-001";
        given(orderRepository.findByOrderNumber(orderNumber)).willReturn(Optional.of(testOrder));

        OrderEntity result = orderMapStore.load(orderNumber);

        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isEqualTo(orderNumber);
        assertThat(result.getId()).isEqualTo(testOrder.getId());
        verify(orderRepository).findByOrderNumber(orderNumber);
    }

    @Test
    @DisplayName("Should return null when order not found")
    void shouldReturnNullWhenOrderNotFound() {
        String orderNumber = "NON-EXISTENT";
        given(orderRepository.findByOrderNumber(orderNumber)).willReturn(Optional.empty());

        OrderEntity result = orderMapStore.load(orderNumber);

        assertThat(result).isNull();
        verify(orderRepository).findByOrderNumber(orderNumber);
    }

    @Test
    @DisplayName("Should handle load exception gracefully")
    void shouldHandleLoadException() {
        String orderNumber = "ORD-001";
        RuntimeException testException = new RuntimeException("Database error");
        given(orderRepository.findByOrderNumber(orderNumber)).willThrow(testException);

        OrderEntity result = orderMapStore.load(orderNumber);

        assertThat(result).isNull();
        verify(orderRepository).findByOrderNumber(orderNumber);
    }

    @Test
    @DisplayName("Should load multiple orders successfully")
    void shouldLoadAllOrdersSuccessfully() {
        Collection<String> orderNumbers = Arrays.asList("ORD-001", "ORD-002", "ORD-003");

        given(orderRepository.findByOrderNumber("ORD-001")).willReturn(Optional.of(testOrder));
        given(orderRepository.findByOrderNumber("ORD-002")).willReturn(Optional.of(anotherTestOrder));
        given(orderRepository.findByOrderNumber("ORD-003")).willReturn(Optional.empty());

        Map<String, OrderEntity> result = orderMapStore.loadAll(orderNumbers);

        assertThat(result).hasSize(2);
        assertThat(result).containsKey("ORD-001");
        assertThat(result).containsKey("ORD-002");
        assertThat(result).doesNotContainKey("ORD-003");
        assertThat(result.get("ORD-001")).isEqualTo(testOrder);
        assertThat(result.get("ORD-002")).isEqualTo(anotherTestOrder);

        verify(orderRepository).findByOrderNumber("ORD-001");
        verify(orderRepository).findByOrderNumber("ORD-002");
        verify(orderRepository).findByOrderNumber("ORD-003");
    }

    @Test
    @DisplayName("Should handle loadAll with empty collection")
    void shouldHandleLoadAllWithEmptyCollection() {
        Collection<String> emptyOrderNumbers = Arrays.asList();

        Map<String, OrderEntity> result = orderMapStore.loadAll(emptyOrderNumbers);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle loadAll exception gracefully")
    void shouldHandleLoadAllException() {
        Collection<String> orderNumbers = Arrays.asList("ORD-001");
        RuntimeException testException = new RuntimeException("Database error");
        given(orderRepository.findByOrderNumber("ORD-001")).willThrow(testException);

        Map<String, OrderEntity> result = orderMapStore.loadAll(orderNumbers);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty set for loadAllKeys")
    void shouldReturnEmptySetForLoadAllKeys() {
        Set<String> result = orderMapStore.loadAllKeys();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle delete operation gracefully")
    void shouldHandleDeleteOperation() {
        String orderNumber = "ORD-001";

        // Should complete without throwing exception
        orderMapStore.delete(orderNumber);

        // Method should log but not actually delete - deletion handled by service layer
    }

    @Test
    @DisplayName("Should handle deleteAll operation gracefully")
    void shouldHandleDeleteAllOperation() {
        Collection<String> orderNumbers = Arrays.asList("ORD-001", "ORD-002");

        // Should complete without throwing exception
        orderMapStore.deleteAll(orderNumbers);

        // Method should log but not actually delete - deletion handled by service layer
    }

    @Test
    @DisplayName("Should handle storeAll operation gracefully")
    void shouldHandleStoreAllOperation() {
        Map<String, OrderEntity> orders = Map.of(
                "ORD-001", testOrder,
                "ORD-002", anotherTestOrder);

        // Should complete without throwing exception
        orderMapStore.storeAll(orders);

        // Method should log completion
    }

    @Test
    @DisplayName("Should handle store operation exception")
    void shouldHandleStoreException() {
        // Create an order that will trigger validation warning
        OrderEntity invalidOrder = createTestOrder("DIFFERENT-ORDER", 999L, "Test", 1);
        String orderNumber = "ORD-001";

        // Should complete without throwing exception but log warning
        orderMapStore.store(orderNumber, invalidOrder);
    }

    @Test
    @DisplayName("Should handle complex order data in load operation")
    void shouldHandleComplexOrderDataInLoad() {
        String orderNumber = "ORD-COMPLEX";
        OrderEntity complexOrder = createComplexTestOrder();
        given(orderRepository.findByOrderNumber(orderNumber)).willReturn(Optional.of(complexOrder));

        OrderEntity result = orderMapStore.load(orderNumber);

        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isEqualTo(orderNumber);
        assertThat(result.getCustomer().name()).isEqualTo("Complex Customer");
        assertThat(result.getOrderItem().quantity()).isEqualTo(5);
        verify(orderRepository).findByOrderNumber(orderNumber);
    }

    // Helper methods for creating test data

    private OrderEntity createTestOrder(String orderNumber, Long id, String productName, int quantity) {
        Customer customer = new Customer("Test Customer", "test@example.com", "123-456-7890");
        OrderItem orderItem = new OrderItem("P001", productName, new BigDecimal("29.99"), quantity);

        return new OrderEntity(
                id,
                orderNumber,
                customer,
                "123 Test Street, Test City",
                orderItem,
                OrderStatus.NEW,
                LocalDateTime.now(),
                null);
    }

    private OrderEntity createComplexTestOrder() {
        Customer complexCustomer = new Customer("Complex Customer", "complex@example.com", "987-654-3210");
        OrderItem complexItem = new OrderItem("P999", "Complex Product", new BigDecimal("199.99"), 5);

        return new OrderEntity(
                999L,
                "ORD-COMPLEX",
                complexCustomer,
                "999 Complex Avenue, Complex City, Complex State 99999",
                complexItem,
                OrderStatus.IN_PROCESS,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now());
    }
}
