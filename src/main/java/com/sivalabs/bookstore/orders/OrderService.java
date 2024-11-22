package com.sivalabs.bookstore.orders;

import com.sivalabs.bookstore.orders.domain.OrderEntity;
import com.sivalabs.bookstore.orders.domain.OrderView;
import java.util.List;
import java.util.Optional;

public interface OrderService {
    OrderEntity createOrder(OrderEntity request);

    Optional<OrderEntity> findOrder(String orderNumber);

    List<OrderView> findOrders();
}
