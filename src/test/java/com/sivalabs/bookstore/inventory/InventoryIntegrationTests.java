package com.sivalabs.bookstore.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.sivalabs.bookstore.TestcontainersConfiguration;
import com.sivalabs.bookstore.inventory.internal.InventoryService;
import com.sivalabs.bookstore.orders.domain.events.OrderCreatedEvent;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

@ApplicationModuleTest(webEnvironment = RANDOM_PORT, classes = TestcontainersConfiguration.class)
class InventoryIntegrationTests {

    @Autowired
    private InventoryService inventoryService;

    @Test
    void handleOrderCreatedEvent(Scenario scenario) {
        scenario.publish(new OrderCreatedEvent(UUID.randomUUID().toString(), "P100", 2, 1L))
                .andWaitAtMost(Duration.ofSeconds(1))
                .andWaitForStateChange(() -> inventoryService.getStockLevel("P100"))
                .andVerify(stockLevel -> assertThat(stockLevel).isEqualTo(98));
    }
}
