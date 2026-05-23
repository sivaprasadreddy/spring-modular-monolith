package com.sivalabs.bookstore.catalog.web;

import com.sivalabs.bookstore.catalog.CreateProductRequest;
import com.sivalabs.bookstore.catalog.domain.DuplicateProductCodeException;
import com.sivalabs.bookstore.catalog.domain.ProductNotFoundException;
import com.sivalabs.bookstore.catalog.domain.ProductService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/new")
    String showCreateForm(Model model) {
        log.info("Admin showing create product form");
        model.addAttribute("product", new CreateProductRequest(null, null, null, null, null));
        return "admin/catalog/product-form";
    }

    @PostMapping
    String createProduct(
            @Valid @ModelAttribute("product") CreateProductRequest request, BindingResult result, Model model) {
        log.info("Admin creating product with code: {}", request.code());
        if (result.hasErrors()) {
            return "admin/catalog/product-form";
        }
        try {
            productService.createProduct(request);
        } catch (DuplicateProductCodeException e) {
            result.rejectValue("code", "duplicate", "A product with this code already exists.");
            return "admin/catalog/product-form";
        }
        return "redirect:/admin/catalog/products/" + request.code();
    }

    @GetMapping("/{code}")
    String getProductByCode(@PathVariable String code, Model model, HtmxRequest hxRequest) {
        log.info("Admin fetching product by code: {}", code);
        var product = productService.getByCode(code).orElseThrow(() -> ProductNotFoundException.forCode(code));
        model.addAttribute("product", product);
        if (hxRequest.isHtmxRequest()) {
            return "partials/admin/catalog/product";
        }
        return "admin/catalog/product";
    }
}
