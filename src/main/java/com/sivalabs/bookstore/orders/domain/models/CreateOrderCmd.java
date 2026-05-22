package com.sivalabs.bookstore.orders.domain.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.jspecify.annotations.Nullable;

public record CreateOrderCmd(
        @Nullable UserId userId,
        @Valid Customer customer,
        @NotEmpty String deliveryAddress,
        @Valid OrderItem item) {

    public CreateOrderCmd withUserId(Long userId) {
        return new CreateOrderCmd(new UserId(userId), customer, deliveryAddress, item);
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
