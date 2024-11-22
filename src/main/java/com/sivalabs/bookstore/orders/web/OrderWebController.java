package com.sivalabs.bookstore.orders.web;

import com.sivalabs.bookstore.catalog.ProductService;
import com.sivalabs.bookstore.orders.OrderNotFoundException;
import com.sivalabs.bookstore.orders.OrderService;
import com.sivalabs.bookstore.orders.domain.OrderEntity;
import com.sivalabs.bookstore.orders.domain.OrderItem;
import com.sivalabs.bookstore.orders.domain.OrderView;
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
class OrderWebController extends OrderWebSupport {
    private static final Logger log = LoggerFactory.getLogger(OrderWebController.class);

    private final OrderService orderService;

    OrderWebController(OrderService orderService, ProductService productService) {
        super(productService);
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    String createOrder(
            @ModelAttribute("orderForm") @Valid OrderForm orderForm,
            BindingResult bindingResult,
            Model model,
            HttpSession session) {
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
        validate(request);
        OrderEntity newOrder = OrderMapper.convertToEntity(request);
        CreateOrderResponse orderResponse =
                new CreateOrderResponse(orderService.createOrder(newOrder).getOrderNumber());
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
    String getOrder(@PathVariable String orderNumber, Model model) {
        log.info("Fetching order by orderNumber: {}", orderNumber);
        OrderDTO orderDTO = orderService
                .findOrder(orderNumber)
                .map(OrderMapper::convertToDTO)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));
        model.addAttribute("order", orderDTO);
        return "order_details";
    }
}
