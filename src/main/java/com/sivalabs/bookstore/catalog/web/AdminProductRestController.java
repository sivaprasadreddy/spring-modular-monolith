package com.sivalabs.bookstore.catalog.web;

import com.sivalabs.bookstore.catalog.ProductDto;
import com.sivalabs.bookstore.catalog.domain.ProductService;
import com.sivalabs.bookstore.common.models.PagedResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
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
}
