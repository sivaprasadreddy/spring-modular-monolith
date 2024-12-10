package com.sivalabs.bookstore.orders;

import com.sivalabs.bookstore.orders.domain.models.Customer;
import com.sivalabs.bookstore.orders.domain.models.OrderStatus;

public record OrderView(String orderNumber, OrderStatus status, Customer customer) {}
