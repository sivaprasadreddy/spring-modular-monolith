package com.sivalabs.bookstore.orders.domain.models;

public record OrderView(String orderNumber, OrderStatus status, CustomerDTO customer) {}
