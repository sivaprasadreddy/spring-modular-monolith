package com.sivalabs.bookstore.orders.domain.events;

public record OrderCreatedEvent(
        String orderNumber,
        String productCode,
        int quantity,
        Long customerId) {
}
