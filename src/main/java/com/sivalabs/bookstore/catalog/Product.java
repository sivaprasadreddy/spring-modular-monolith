package com.sivalabs.bookstore.catalog;

import java.math.BigDecimal;

public record Product(String code, String name, String description, String imageUrl, BigDecimal price) {
    public String getDisplayName() {
        if (name.length() <= 20) {
            return name;
        }
        return name.substring(0, 20) + "...";
    }
}
