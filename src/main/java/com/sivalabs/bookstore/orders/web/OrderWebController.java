package com.sivalabs.bookstore.orders.web;

import com.sivalabs.bookstore.orders.CreateOrderRequest;
import com.sivalabs.bookstore.orders.OrderDto;
import com.sivalabs.bookstore.orders.OrderNotFoundException;
import com.sivalabs.bookstore.orders.OrderView;
import com.sivalabs.bookstore.orders.domain.OrderEntity;
import com.sivalabs.bookstore.orders.domain.OrderService;
import com.sivalabs.bookstore.orders.domain.ProductServiceClient;
import com.sivalabs.bookstore.orders.domain.models.*;
import com.sivalabs.bookstore.orders.mappers.OrderMapper;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
class OrderWebController {
    private static final Logger log = LoggerFactory.getLogger(OrderWebController.class);

    private final OrderService orderService;
    private final ProductServiceClient productServiceClient;

    OrderWebController(OrderService orderService, ProductServiceClient productServiceClient) {
        this.orderService = orderService;
        this.productServiceClient = productServiceClient;
    }

    @PostMapping("/orders")
    String createOrder(
            @ModelAttribute @Valid OrderForm orderForm, BindingResult bindingResult, Model model, HttpSession session) {
        Cart cart = CartUtil.getCart(session);
        if (bindingResult.hasErrors()) {
            model.addAttribute("cart", cart);
            return "cart";
        }
        Cart.LineItem lineItem = cart.getItem();
        OrderItem orderItem = new OrderItem(
                lineItem.getCode(), lineItem.getName(),
                lineItem.getPrice(), lineItem.getQuantity());
        var request = new CreateOrderRequest(orderForm.customer(), orderForm.deliveryAddress(), orderItem);
        productServiceClient.validate(request.item().code(), request.item().price());
        OrderEntity newOrder = OrderMapper.convertToEntity(request);
        var savedOrder = orderService.createOrder(newOrder);
        session.removeAttribute("cart");
        return "redirect:/orders/" + savedOrder.getOrderNumber();
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
        List<OrderView> orders = OrderMapper.convertToOrderViews(orderService.findOrders());
        model.addAttribute("orders", orders);
    }

    @GetMapping("/orders/{orderNumber}")
    String getOrder(@PathVariable String orderNumber, Model model) {
        log.info("Fetching order by orderNumber: {}", orderNumber);
        OrderDto orderDto = orderService
                .findOrder(orderNumber)
                .map(OrderMapper::convertToDto)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));
        model.addAttribute("order", orderDto);
        return "order_details";
    }
}
