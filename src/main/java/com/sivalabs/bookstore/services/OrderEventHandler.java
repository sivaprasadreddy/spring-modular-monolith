package com.sivalabs.bookstore.services;

import com.sivalabs.bookstore.models.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class OrderEventHandler {
    private static final Logger log = LoggerFactory.getLogger(OrderEventHandler.class);

    @EventListener
    void handle(OrderCreatedEvent event) {
        log.info("[Notification]: Received order created event: {}", event);
    }
}
