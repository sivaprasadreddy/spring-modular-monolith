package com.sivalabs.bookstore.web.controllers;

import com.sivalabs.bookstore.exceptions.ProductNotFoundException;
import com.sivalabs.bookstore.models.PagedResult;
import com.sivalabs.bookstore.models.Product;
import com.sivalabs.bookstore.services.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
class ProductRestController {
    private static final Logger log = LoggerFactory.getLogger(ProductRestController.class);

    private final ProductService productService;

    ProductRestController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    PagedResult<Product> getProducts(@RequestParam(name = "page", defaultValue = "1") int page) {
        log.info("Fetching products for page: {}", page);
        return productService.getProducts(page);
    }

    @GetMapping("/{code}")
    Product getProductByCode(@PathVariable String code) {
        log.info("Fetching product by code: {}", code);
        return productService.getByCode(code).orElseThrow(() -> ProductNotFoundException.forCode(code));
    }
}
