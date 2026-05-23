package com.sivalabs.bookstore.orders.web;

import com.sivalabs.bookstore.orders.domain.models.OrderStatus;
import jakarta.validation.constraints.NotNull;

record UpdateOrderStatusRequest(@NotNull OrderStatus status) {}
