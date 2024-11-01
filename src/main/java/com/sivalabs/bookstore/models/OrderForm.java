package com.sivalabs.bookstore.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record OrderForm(@Valid Customer customer, @NotEmpty String deliveryAddress) {}
