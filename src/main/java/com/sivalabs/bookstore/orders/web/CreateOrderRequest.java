package com.sivalabs.bookstore.orders.web;

import com.sivalabs.bookstore.orders.domain.Customer;
import com.sivalabs.bookstore.orders.domain.OrderItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record CreateOrderRequest(@Valid Customer customer, @NotEmpty String deliveryAddress, @Valid OrderItem item) {}
