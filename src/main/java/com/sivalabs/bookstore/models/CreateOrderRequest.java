package com.sivalabs.bookstore.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record CreateOrderRequest(@Valid Customer customer, @NotEmpty String deliveryAddress, @Valid OrderItem item) {}
