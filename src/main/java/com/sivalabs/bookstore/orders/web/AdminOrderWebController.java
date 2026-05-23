package com.sivalabs.bookstore.orders.web;

import com.sivalabs.bookstore.orders.domain.OrderNotFoundException;
import com.sivalabs.bookstore.orders.domain.OrderService;
import com.sivalabs.bookstore.orders.domain.models.OrderStatus;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/orders")
class AdminOrderWebController {
    private static final Logger log = LoggerFactory.getLogger(AdminOrderWebController.class);

    private final OrderService orderService;

    AdminOrderWebController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    String showOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam @Nullable OrderStatus status,
            Model model,
            HtmxRequest hxRequest) {
        log.info("Admin fetching orders for page: {}, status: {}", page, status);
        model.addAttribute("ordersPage", orderService.getOrdersAdmin(page, status));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", OrderStatus.values());
        if (hxRequest.isHtmxRequest()) {
            return "partials/admin/orders";
        }
        return "admin/orders";
    }

    @GetMapping("/{orderNumber}")
    String showOrder(@PathVariable String orderNumber, Model model, HtmxRequest hxRequest) {
        log.info("Admin fetching order by orderNumber: {}", orderNumber);
        var order = orderService
                .findOrderAdmin(orderNumber)
                .orElseThrow(() -> OrderNotFoundException.forOrderNumber(orderNumber));
        model.addAttribute("order", order);
        if (hxRequest.isHtmxRequest()) {
            return "partials/admin/order";
        }
        return "admin/order";
    }
}
