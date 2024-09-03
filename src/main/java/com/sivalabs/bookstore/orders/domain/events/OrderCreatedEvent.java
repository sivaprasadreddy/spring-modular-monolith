package com.sivalabs.bookstore.orders.domain.events;

import org.springframework.modulith.events.Externalized;

@Externalized("BookStoreExchange::orders.new")
public record OrderCreatedEvent(String orderNumber, String productCode, int quantity, Long customerId) {}
