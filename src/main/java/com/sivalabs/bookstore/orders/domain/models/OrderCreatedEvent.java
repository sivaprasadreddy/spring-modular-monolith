package com.sivalabs.bookstore.orders.domain.models;

public record OrderCreatedEvent(String orderNumber, String productCode, int quantity, Customer customer) {}
