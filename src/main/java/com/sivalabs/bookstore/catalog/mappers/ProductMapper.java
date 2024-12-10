package com.sivalabs.bookstore.catalog.mappers;

import com.sivalabs.bookstore.catalog.ProductDto;
import com.sivalabs.bookstore.catalog.domain.ProductEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductDto mapToDto(ProductEntity entity) {
        return new ProductDto(
                entity.getCode(), entity.getName(), entity.getDescription(), entity.getImageUrl(), entity.getPrice());
    }
}
