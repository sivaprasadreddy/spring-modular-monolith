package com.sivalabs.bookstore.orders.domain.models;

public record OrderSummary(String orderNumber, Long customerId, OrderStatus status) {}
