package com.sivalabs.bookstore.orders;

import com.sivalabs.bookstore.orders.domain.models.Customer;
import com.sivalabs.bookstore.orders.domain.models.OrderItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.jspecify.annotations.Nullable;

public record CreateOrderRequest(
        @Nullable UserId userId,
        @Valid Customer customer,
        @NotEmpty String deliveryAddress,
        @Valid OrderItem item) {

    public CreateOrderRequest withUserId(Long userId) {
        return new CreateOrderRequest(new UserId(userId), customer, deliveryAddress, item);
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
