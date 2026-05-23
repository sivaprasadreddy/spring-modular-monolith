package com.sivalabs.bookstore.catalog.domain;

public class DuplicateProductCodeException extends RuntimeException {
    public DuplicateProductCodeException(String message) {
        super(message);
    }

    public static DuplicateProductCodeException forCode(String code) {
        return new DuplicateProductCodeException("Product with code " + code + " already exists");
    }
}
