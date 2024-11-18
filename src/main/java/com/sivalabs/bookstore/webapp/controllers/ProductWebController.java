package com.sivalabs.bookstore.webapp.controllers;

import com.sivalabs.bookstore.catalog.Product;
import com.sivalabs.bookstore.catalog.ProductService;
import com.sivalabs.bookstore.common.models.PagedResult;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
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

    ProductWebController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    String index() {
        return "redirect:/products";
    }

    @GetMapping("/products")
    String showProducts(@RequestParam(defaultValue = "1") int page, Model model) {
        log.info("Fetching products for page: {}", page);
        PagedResult<Product> productsPage = productService.getProducts(page);
        model.addAttribute("productsPage", productsPage);
        return "products";
    }

    @HxRequest
    @GetMapping("/partials/products")
    String getProductsPartial(@RequestParam(defaultValue = "1") int page, Model model) {
        log.info("Fetching products-partial for page: {}", page);
        PagedResult<Product> productsPage = productService.getProducts(page);
        model.addAttribute("productsPage", productsPage);
        return "partials/products";
    }
}
