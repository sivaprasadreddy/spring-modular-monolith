package com.sivalabs.bookstore.catalog.web;

import com.sivalabs.bookstore.catalog.domain.ProductService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/catalog/products")
class AdminProductWebController {
    private static final Logger log = LoggerFactory.getLogger(AdminProductWebController.class);

    private final ProductService productService;

    AdminProductWebController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    String showProducts(@RequestParam(defaultValue = "1") int page, Model model, HtmxRequest hxRequest) {
        log.info("Admin fetching products for page: {}", page);
        model.addAttribute("productsPage", productService.getProducts(page));
        if (hxRequest.isHtmxRequest()) {
            return "partials/admin/catalog/products";
        }
        return "admin/catalog/products";
    }
}
