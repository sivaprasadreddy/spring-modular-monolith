package com.sivalabs.bookstore.orders.domain.models;

import java.time.LocalDateTime;

public record OrderDTO(
        String orderNumber,
        OrderItem item,
        CustomerDTO customer,
        String deliveryAddress,
        OrderStatus status,
        LocalDateTime createdAt) {}
