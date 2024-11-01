package com.sivalabs.bookstore.orders.domain.events;

import com.sivalabs.bookstore.orders.domain.models.Customer;

public record OrderCreatedEvent(String orderNumber, String productCode, int quantity, Customer customer) {}
