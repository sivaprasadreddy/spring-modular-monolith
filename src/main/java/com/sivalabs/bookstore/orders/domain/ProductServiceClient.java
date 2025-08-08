package com.sivalabs.bookstore.orders.domain;

import com.sivalabs.bookstore.catalog.ProductApi;
import com.sivalabs.bookstore.orders.InvalidOrderException;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class ProductServiceClient {
    private final ProductApi productApi;

    public ProductServiceClient(ProductApi productApi) {
        this.productApi = productApi;
    }

    public void validate(String productCode, BigDecimal price) {
        var product = productApi
                .getByCode(productCode)
                .orElseThrow(() -> new InvalidOrderException("Product not found with code: " + productCode));
        if (product.price().compareTo(price) != 0) {
            throw new InvalidOrderException("Product price mismatch");
        }
    }
}
