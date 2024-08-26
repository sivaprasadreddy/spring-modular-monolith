package com.sivalabs.bookstore.orders.domain.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotNull
        Long customerId,
        @NotEmpty
        String deliveryAddress,
        @Valid OrderItem item) {}
