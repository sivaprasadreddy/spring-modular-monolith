package com.sivalabs.bookstore.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.sivalabs.bookstore.TestcontainersConfiguration;
import com.sivalabs.bookstore.orders.domain.events.OrderCreatedEvent;
import com.sivalabs.bookstore.orders.domain.models.Customer;
import java.time.Duration;
import java.util.UUID;
import org.awaitility.Awaitility;
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
        var customer = new Customer("Siva", "siva@gmail.com", "9987654");
        String productCode = "P114";
        var event = new OrderCreatedEvent(UUID.randomUUID().toString(), productCode, 2, customer);
        /*
                scenario.publish(event)
                        .andWaitForStateChange(() -> inventoryService.getStockLevel("P114"))
                        .andVerify(stockLevel -> assertThat(stockLevel).isEqualTo(598));
        */
        scenario.publish(event).andWaitForStateChange(() -> productCode).andVerify(code -> Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    Long stockLevel = inventoryService.getStockLevel(code);
                    assertThat(stockLevel).isEqualTo(598);
                }));
    }
}
