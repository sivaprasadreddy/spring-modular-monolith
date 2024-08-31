package com.sivalabs.bookstore.inventory;

import com.sivalabs.bookstore.orders.domain.events.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
class InventoryEventHandler {
    private static final Logger log = LoggerFactory.getLogger(InventoryEventHandler.class);

    @ApplicationModuleListener
    void handle(OrderCreatedEvent event) {
        log.info("[Inventory]: Received order created event: {}", event);
    }
}
