package com.sivalabs.bookstore.catalog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;

public record ProductDto(String code, String name, String description, String imageUrl, BigDecimal price) {
    @JsonIgnore
    public String getDisplayName() {
        if (name.length() <= 20) {
            return name;
        }
        return name.substring(0, 20) + "...";
    }
}
