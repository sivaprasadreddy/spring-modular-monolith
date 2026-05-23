package com.sivalabs.bookstore.orders.domain.models;

public enum OrderStatus {
    NEW,
    IN_PROCESS,
    DELIVERED,
    CANCELLED,
    ERROR;

    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case NEW -> next == IN_PROCESS || next == CANCELLED;
            case IN_PROCESS -> next == DELIVERED || next == CANCELLED;
            default -> false;
        };
    }
}
