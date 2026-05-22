package com.sivalabs.bookstore.catalog.domain;

import com.sivalabs.bookstore.catalog.ProductDto;
import org.springframework.stereotype.Component;

@Component
class ProductMapper {

    public ProductDto mapToDto(ProductEntity entity) {
        return new ProductDto(
                entity.getCode(), entity.getName(), entity.getDescription(), entity.getImageUrl(), entity.getPrice());
    }
}
