package com.sivalabs.bookstore.customers.domain;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String message) {
        super(message);
    }

    public static CustomerNotFoundException forId(Long customerId) {
        return new CustomerNotFoundException("Customer with id " + customerId + " not found");
    }
}
