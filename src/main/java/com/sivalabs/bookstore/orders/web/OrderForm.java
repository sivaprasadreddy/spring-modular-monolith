package com.sivalabs.bookstore.orders.web;

import com.sivalabs.bookstore.orders.domain.models.Customer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record OrderForm(
        @Valid Customer customer, @NotEmpty(message = "Delivery address is required") String deliveryAddress) {}
