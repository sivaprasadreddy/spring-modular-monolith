package com.sivalabs.bookstore.inventory.domain;

public class InvalidInventoryException extends RuntimeException {
    InvalidInventoryException(String message) {
        super(message);
    }

    static InvalidInventoryException negativeQuantity(long quantity) {
        return new InvalidInventoryException("Quantity must be non-negative, but was: " + quantity);
    }
}
