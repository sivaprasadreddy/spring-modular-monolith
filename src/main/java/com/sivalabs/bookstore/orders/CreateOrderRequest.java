package com.sivalabs.bookstore.orders;

import com.sivalabs.bookstore.orders.domain.models.Customer;
import com.sivalabs.bookstore.orders.domain.models.OrderItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record CreateOrderRequest(
        UserId userId, @Valid Customer customer, @NotEmpty String deliveryAddress, @Valid OrderItem item) {

    public CreateOrderRequest {
        if (userId == null) {
            userId = new UserId(null);
        }
    }

    public void withUserId(Long userId) {
        this.userId.setUserId(userId);
    }

    public static class UserId {
        private Long userId;

        public UserId(Long userId) {
            this.userId = userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getUserId() {
            return userId;
        }
    }
}
