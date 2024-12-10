package com.sivalabs.bookstore.catalog.web;

import com.sivalabs.bookstore.catalog.ProductDto;
import com.sivalabs.bookstore.catalog.domain.ProductNotFoundException;
import com.sivalabs.bookstore.catalog.domain.ProductService;
import com.sivalabs.bookstore.catalog.mappers.ProductMapper;
import com.sivalabs.bookstore.common.models.PagedResult;
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
    private final ProductMapper productMapper;

    ProductRestController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @GetMapping
    PagedResult<ProductDto> getProducts(@RequestParam(defaultValue = "1") int page) {
        log.info("Fetching products for page: {}", page);
        var pagedResult = productService.getProducts(page);
        return PagedResult.of(pagedResult, productMapper::mapToDto);
    }

    @GetMapping("/{code}")
    ProductDto getProductByCode(@PathVariable String code) {
        log.info("Fetching product by code: {}", code);
        return productService
                .getByCode(code)
                .map(productMapper::mapToDto)
                .orElseThrow(() -> ProductNotFoundException.forCode(code));
    }
}
