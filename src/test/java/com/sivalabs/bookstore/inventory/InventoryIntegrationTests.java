package com.sivalabs.bookstore.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.sivalabs.bookstore.TestcontainersConfiguration;
import com.sivalabs.bookstore.orders.domain.events.OrderCreatedEvent;
import com.sivalabs.bookstore.orders.domain.models.Customer;
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
        scenario.publish(new OrderCreatedEvent(
                        UUID.randomUUID().toString(), "P100", 2, new Customer("Siva", "siva@gmail.com", "9987654")))
                .andWaitAtMost(Duration.ofSeconds(1))
                .andWaitForStateChange(() -> inventoryService.getStockLevel("P100"))
                .andVerify(stockLevel -> assertThat(stockLevel).isEqualTo(98));
    }
}
