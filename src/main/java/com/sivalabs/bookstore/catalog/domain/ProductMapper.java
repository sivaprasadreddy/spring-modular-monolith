package com.sivalabs.bookstore.catalog.domain;

import com.sivalabs.bookstore.catalog.CreateProductRequest;
import com.sivalabs.bookstore.catalog.ProductDto;
import com.sivalabs.bookstore.catalog.UpdateProductRequest;
import org.springframework.stereotype.Component;

@Component
class ProductMapper {

    public ProductDto mapToDto(ProductEntity entity) {
        return new ProductDto(
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getImageUrl(),
                entity.getPrice(),
                entity.getDeletedAt());
    }

    public ProductEntity mapToEntity(CreateProductRequest request) {
        ProductEntity entity = new ProductEntity();
        entity.setCode(request.code());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setImageUrl(request.imageUrl());
        entity.setPrice(request.price());
        return entity;
    }

    public void updateEntity(ProductEntity entity, UpdateProductRequest request) {
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setImageUrl(request.imageUrl());
        entity.setPrice(request.price());
    }
}
