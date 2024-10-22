package com.sivalabs.bookstore.orders;

import com.sivalabs.bookstore.orders.domain.models.CreateOrderRequest;
import com.sivalabs.bookstore.orders.domain.models.CreateOrderResponse;
import com.sivalabs.bookstore.orders.domain.models.OrderDTO;
import com.sivalabs.bookstore.orders.domain.models.OrderView;
import java.util.List;
import java.util.Optional;

public interface OrderService {
    CreateOrderResponse createOrder(CreateOrderRequest request);

    Optional<OrderDTO> findOrder(String orderNumber);

    List<OrderView> findOrders();
}
