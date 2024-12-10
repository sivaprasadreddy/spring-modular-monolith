package com.sivalabs.bookstore.orders;

import com.sivalabs.bookstore.orders.domain.models.Customer;
import com.sivalabs.bookstore.orders.domain.models.OrderItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record CreateOrderRequest(@Valid Customer customer, @NotEmpty String deliveryAddress, @Valid OrderItem item) {}
