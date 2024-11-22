package com.sivalabs.bookstore.orders.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sivalabs.bookstore.orders.domain.Customer;
import com.sivalabs.bookstore.orders.domain.OrderItem;
import com.sivalabs.bookstore.orders.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderDTO(
        String orderNumber,
        OrderItem item,
        Customer customer,
        String deliveryAddress,
        OrderStatus status,
        LocalDateTime createdAt) {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public BigDecimal getTotalAmount() {
        return item.price().multiply(BigDecimal.valueOf(item.quantity()));
    }
}
