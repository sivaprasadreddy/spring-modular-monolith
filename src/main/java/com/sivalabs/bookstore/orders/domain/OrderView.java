package com.sivalabs.bookstore.orders.domain;

public record OrderView(String orderNumber, OrderStatus status, Customer customer) {}
