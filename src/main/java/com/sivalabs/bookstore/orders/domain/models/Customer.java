package com.sivalabs.bookstore.orders.domain.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

public record Customer(
        @NotBlank(message = "Customer Name is required") String name,
        @NotBlank(message = "Customer email is required") @Email String email,
        @NotBlank(message = "Customer Phone number is required") String phone)
        implements Serializable {

    private static final long serialVersionUID = 1L;
}
