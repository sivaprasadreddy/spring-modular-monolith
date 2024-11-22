package com.sivalabs.bookstore.inventory;

import com.sivalabs.bookstore.orders.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
class InventoryEventHandler {
    private static final Logger log = LoggerFactory.getLogger(InventoryEventHandler.class);
    private final InventoryService inventoryService;

    InventoryEventHandler(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @ApplicationModuleListener
    void handle(OrderCreatedEvent event) {
        log.info("[Inventory]: Received order created event: {}", event);
        inventoryService.decreaseStockLevel(event.productCode(), event.quantity());
    }
}
