package com.sivalabs.bookstore.catalog;

import com.sivalabs.bookstore.catalog.domain.ProductService;
import com.sivalabs.bookstore.catalog.mappers.ProductMapper;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ProductApi {
    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductApi(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    public Optional<ProductDto> getByCode(String code) {
        return productService.getByCode(code).map(productMapper::mapToDto);
    }
}
