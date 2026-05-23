package com.sivalabs.bookstore.catalog.web;

import com.sivalabs.bookstore.catalog.CreateProductRequest;
import com.sivalabs.bookstore.catalog.ProductDto;
import com.sivalabs.bookstore.catalog.domain.ProductNotFoundException;
import com.sivalabs.bookstore.catalog.domain.ProductService;
import com.sivalabs.bookstore.common.models.PagedResult;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/catalog/products")
class AdminProductRestController {
    private static final Logger log = LoggerFactory.getLogger(AdminProductRestController.class);

    private final ProductService productService;

    AdminProductRestController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    PagedResult<ProductDto> getProducts(@RequestParam(defaultValue = "1") int page) {
        log.info("Admin fetching products for page: {}", page);
        return productService.getProducts(page);
    }

    @GetMapping("/{code}")
    ProductDto getProductByCode(@PathVariable String code) {
        log.info("Admin fetching product by code: {}", code);
        return productService.getByCode(code).orElseThrow(() -> ProductNotFoundException.forCode(code));
    }

    @PostMapping
    ResponseEntity<ProductDto> createProduct(@Valid @RequestBody CreateProductRequest request) {
        log.info("Admin creating product with code: {}", request.code());
        ProductDto created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
