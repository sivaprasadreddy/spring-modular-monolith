package com.sivalabs.bookstore.catalog.web;

import com.sivalabs.bookstore.catalog.domain.ProductService;
import com.sivalabs.bookstore.catalog.mappers.ProductMapper;
import com.sivalabs.bookstore.common.models.PagedResult;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class ProductWebController {
    private static final Logger log = LoggerFactory.getLogger(ProductWebController.class);

    private final ProductService productService;
    private final ProductMapper productMapper;

    ProductWebController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @GetMapping
    String index() {
        return "redirect:/products";
    }

    @GetMapping("/products")
    String showProducts(@RequestParam(defaultValue = "1") int page, Model model, HtmxRequest hxRequest) {
        log.info("Fetching products for page: {}", page);
        var pagedResult = productService.getProducts(page);
        var productsPage = PagedResult.of(pagedResult, productMapper::mapToDto);
        model.addAttribute("productsPage", productsPage);
        if (hxRequest.isHtmxRequest()) {
            return "partials/products";
        }
        return "products";
    }
}
