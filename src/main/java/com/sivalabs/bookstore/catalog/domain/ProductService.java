package com.sivalabs.bookstore.catalog.domain;

import com.sivalabs.bookstore.catalog.ProductDto;
import com.sivalabs.bookstore.common.models.PagedResult;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private static final int PRODUCT_PAGE_SIZE = 10;
    private final ProductRepository repo;
    private final ProductMapper productMapper;

    ProductService(ProductRepository repo, ProductMapper productMapper) {
        this.repo = repo;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public PagedResult<ProductDto> getProducts(int pageNo) {
        Sort sort = Sort.by("name").ascending();
        int page = pageNo <= 1 ? 0 : pageNo - 1;
        Pageable pageable = PageRequest.of(page, PRODUCT_PAGE_SIZE, sort);
        Page<ProductDto> productsPage = repo.findAll(pageable).map(productMapper::mapToDto);
        return new PagedResult<>(productsPage);
    }

    @Transactional(readOnly = true)
    public Optional<ProductDto> getByCode(String code) {
        return repo.findByCode(code).map(productMapper::mapToDto);
    }
}
