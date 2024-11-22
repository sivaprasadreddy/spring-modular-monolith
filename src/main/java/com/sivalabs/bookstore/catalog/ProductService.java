package com.sivalabs.bookstore.catalog;

import com.sivalabs.bookstore.common.PagedResult;
import java.util.Optional;

public interface ProductService {
    PagedResult<Product> getProducts(int pageNo);

    Optional<Product> getByCode(String code);
}
