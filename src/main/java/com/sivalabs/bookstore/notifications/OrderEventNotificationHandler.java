package com.sivalabs.bookstore.notifications;

import com.sivalabs.bookstore.orders.domain.events.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
class OrderEventNotificationHandler {
    private static final Logger log = LoggerFactory.getLogger(OrderEventNotificationHandler.class);

    @ApplicationModuleListener
    void handle(OrderCreatedEvent event) {
        log.info("[Notification]: Received order created event: {}", event);
    }
}
