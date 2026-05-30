package com.sivalabs.bookstore.catalog.domain;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

public record CreateProductCmd(
        @NotBlank(message = "Product code is required") String code,
        @NotBlank(message = "Product name is required") String name,
        @Nullable String description,
        @Nullable String imageUrl,

        @NotNull(message = "Product price is required") @DecimalMin(value = "0.1", message = "Price must be at least 0.1") BigDecimal price) {}
