package com.sivalabs.bookstore.web.controllers;

import com.sivalabs.bookstore.models.Cart;
import com.sivalabs.bookstore.models.Product;
import com.sivalabs.bookstore.services.ProductService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRefreshView;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.FragmentsRendering;

@Controller
class CartController {
    private static final Logger log = LoggerFactory.getLogger(CartController.class);
    private final ProductService productService;

    CartController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/buy")
    String addProductToCart(@RequestParam("code") String code, HttpSession session) {
        log.info("Adding product code:{} to cart", code);
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
        }
        Product product = productService.getByCode(code).orElseThrow();
        cart.setItem(new Cart.LineItem(product.code(), product.name(), product.price(), 1));
        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }

    @GetMapping("/cart")
    String showCart(Model model, HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
        }
        model.addAttribute("cart", cart);
        return "cart";
    }

    @HxRequest
    @PostMapping("/update-cart")
    View updateCart(@RequestParam("code") String code, @RequestParam("quantity") int quantity, HttpSession session) {
        log.info("Updating cart code:{}, quantity:{}", code, quantity);
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
        }
        cart.updateItemQuantity(quantity);
        session.setAttribute("cart", cart);
        boolean refresh = cart.getItem() == null;
        if (refresh) {
            return new HtmxRefreshView();
        }
        return FragmentsRendering.with("partials/cart", Map.of("cart", cart)).build();
    }
}
