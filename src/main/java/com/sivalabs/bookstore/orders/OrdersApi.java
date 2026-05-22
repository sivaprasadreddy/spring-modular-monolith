package com.sivalabs.bookstore.orders;

import com.sivalabs.bookstore.orders.domain.OrderEntity;
import com.sivalabs.bookstore.orders.domain.OrderMapper;
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
        OrderEntity orderEntity = OrderMapper.convertToEntity(cmd);
        var order = orderService.createOrder(orderEntity);
        return new CreateOrderResult(order.getOrderNumber());
    }

    public Optional<OrderDto> findOrder(String orderNumber, Long userId) {
        return orderService.findOrder(orderNumber, userId);
    }

    public List<OrderView> findOrders(Long userId) {
        return orderService.findOrders(userId);
    }
}
