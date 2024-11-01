package com.sivalabs.bookstore.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.sivalabs.bookstore.TestcontainersConfiguration;
import com.sivalabs.bookstore.models.Customer;
import com.sivalabs.bookstore.models.OrderCreatedEvent;
import java.time.Duration;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = TestcontainersConfiguration.class)
class InventoryIntegrationTests {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private InventoryService inventoryService;

    @Test
    void handleOrderCreatedEvent() {
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
                UUID.randomUUID().toString(), "P100", 2, new Customer("Siva", "siva@gmail.com", "9987654"));
        eventPublisher.publishEvent(orderCreatedEvent);

        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            assertThat(inventoryService.getStockLevel("P100")).isEqualTo(98);
        });
    }
}
