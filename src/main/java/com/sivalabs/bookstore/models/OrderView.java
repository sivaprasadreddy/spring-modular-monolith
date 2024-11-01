package com.sivalabs.bookstore.models;

public record OrderView(String orderNumber, OrderStatus status, Customer customer) {}
