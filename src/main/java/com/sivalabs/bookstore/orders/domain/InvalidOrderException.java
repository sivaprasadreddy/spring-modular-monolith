package com.sivalabs.bookstore.orders.domain;

public class InvalidOrderException extends RuntimeException {

    public InvalidOrderException(String message) {
        super(message);
    }

    public static InvalidOrderException invalidTransition(
            com.sivalabs.bookstore.orders.domain.models.OrderStatus from,
            com.sivalabs.bookstore.orders.domain.models.OrderStatus to) {
        return new InvalidOrderException("Order status cannot be changed from " + from + " to " + to);
    }
}
