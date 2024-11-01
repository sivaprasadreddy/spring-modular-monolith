package com.sivalabs.bookstore.models;

public record OrderCreatedEvent(String orderNumber, String productCode, int quantity, Customer customer) {}
