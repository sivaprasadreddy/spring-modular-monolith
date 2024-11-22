package com.sivalabs.bookstore.orders.web;

import com.sivalabs.bookstore.catalog.ProductService;
import com.sivalabs.bookstore.orders.InvalidOrderException;

class OrderWebSupport {

    private final ProductService productService;

    protected OrderWebSupport(ProductService productService) {
        this.productService = productService;
    }

    protected void validate(CreateOrderRequest request) {
        String code = request.item().code();
        var product = productService
                .getByCode(code)
                .orElseThrow(() -> new InvalidOrderException("Product not found with code: " + code));
        if (product.price().compareTo(request.item().price()) != 0) {
            throw new InvalidOrderException("Product price mismatch");
        }
    }
}
