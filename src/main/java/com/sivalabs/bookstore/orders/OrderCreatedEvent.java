package com.sivalabs.bookstore.orders;

import com.sivalabs.bookstore.orders.domain.Customer;
import org.springframework.modulith.events.Externalized;

@Externalized("BookStoreExchange::orders.new")
public record OrderCreatedEvent(String orderNumber, String productCode, int quantity, Customer customer) {}
