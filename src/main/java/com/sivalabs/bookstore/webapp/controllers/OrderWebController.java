package com.sivalabs.bookstore.webapp.controllers;

import com.sivalabs.bookstore.orders.domain.OrderNotFoundException;
import com.sivalabs.bookstore.orders.domain.OrderService;
import com.sivalabs.bookstore.orders.domain.models.*;
import com.sivalabs.bookstore.webapp.models.Cart;
import com.sivalabs.bookstore.webapp.models.OrderForm;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
class OrderWebController {
    private static final Logger log = LoggerFactory.getLogger(OrderWebController.class);

    private final OrderService orderService;

    OrderWebController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    String createOrder(@Valid OrderForm orderForm, HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        Cart.LineItem lineItem = cart.getItem();
        OrderItem orderItem = new OrderItem(
                lineItem.getCode(), lineItem.getName(),
                lineItem.getPrice(), lineItem.getQuantity());
        var request = new CreateOrderRequest(orderForm.customer(), orderForm.deliveryAddress(), orderItem);
        CreateOrderResponse orderResponse = orderService.createOrder(request);
        session.removeAttribute("cart");
        return "redirect:/orders/" + orderResponse.orderNumber();
    }

    @GetMapping("/orders")
    String getOrders(Model model, HtmxRequest hxRequest) {
        fetchOrders(model);
        if (hxRequest.isHtmxRequest()) {
            return "partials/orders";
        }
        return "orders";
    }

    private void fetchOrders(Model model) {
        List<OrderView> orders = orderService.findOrders();
        model.addAttribute("orders", orders);
    }

    @GetMapping("/orders/{orderNumber}")
    String getOrder(@PathVariable("orderNumber") String orderNumber, Model model) {
        log.info("Fetching order by orderNumber: {}", orderNumber);
        OrderDTO orderDTO =
                orderService.findOrder(orderNumber).orElseThrow(() -> new OrderNotFoundException(orderNumber));
        model.addAttribute("order", orderDTO);
        return "order_details";
    }
}
