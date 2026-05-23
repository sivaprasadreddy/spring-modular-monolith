package com.sivalabs.bookstore.orders.domain.models;

import java.time.LocalDateTime;

public record AdminOrderView(String orderNumber, OrderStatus status, String customerName, LocalDateTime createdAt) {}
