package com.sivalabs.bookstore.orders;

import com.sivalabs.bookstore.orders.domain.OrderService;
import com.sivalabs.bookstore.orders.domain.models.CreateOrderCmd;
import com.sivalabs.bookstore.orders.domain.models.CreateOrderResult;
import com.sivalabs.bookstore.orders.domain.models.OrderDto;
import com.sivalabs.bookstore.orders.domain.models.OrderView;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class OrdersApi {
    private final OrderService orderService;

    public OrdersApi(OrderService orderService) {
        this.orderService = orderService;
    }

    public CreateOrderResult createOrder(CreateOrderCmd cmd) {
        return orderService.createOrder(cmd);
    }

    public Optional<OrderDto> findOrder(String orderNumber, Long userId) {
        return orderService.findOrder(orderNumber, userId);
    }

    public List<OrderView> findOrders(Long userId) {
        return orderService.findOrders(userId);
    }
}
