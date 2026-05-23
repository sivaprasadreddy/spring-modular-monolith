package com.sivalabs.bookstore.catalog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record ProductDto(
        String code,
        String name,
        @Nullable String description,
        @Nullable String imageUrl,
        BigDecimal price,
        @Nullable Instant deletedAt) {
    @JsonIgnore
    public String getDisplayName() {
        if (name.length() <= 20) {
            return name;
        }
        return name.substring(0, 20) + "...";
    }
}
