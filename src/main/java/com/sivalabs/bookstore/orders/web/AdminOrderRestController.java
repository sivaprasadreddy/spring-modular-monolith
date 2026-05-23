package com.sivalabs.bookstore.orders.web;

import com.sivalabs.bookstore.common.models.PagedResult;
import com.sivalabs.bookstore.orders.domain.OrderService;
import com.sivalabs.bookstore.orders.domain.models.AdminOrderView;
import com.sivalabs.bookstore.orders.domain.models.OrderStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
class AdminOrderRestController {
    private static final Logger log = LoggerFactory.getLogger(AdminOrderRestController.class);

    private final OrderService orderService;

    AdminOrderRestController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    PagedResult<AdminOrderView> getOrders(
            @RequestParam(defaultValue = "1") int page, @RequestParam @Nullable OrderStatus status) {
        log.info("Admin fetching orders for page: {}, status: {}", page, status);
        return orderService.getOrdersAdmin(page, status);
    }
}
