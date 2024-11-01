package com.sivalabs.bookstore.models;

import com.fasterxml.jackson.annotation.JsonProperty;
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
