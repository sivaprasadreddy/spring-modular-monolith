package com.sivalabs.bookstore.orders.web;

import com.sivalabs.bookstore.orders.CreateOrderRequest;
import com.sivalabs.bookstore.orders.CreateOrderResponse;
import com.sivalabs.bookstore.orders.OrderDto;
import com.sivalabs.bookstore.orders.OrderNotFoundException;
import com.sivalabs.bookstore.orders.OrderView;
import com.sivalabs.bookstore.orders.domain.OrderEntity;
import com.sivalabs.bookstore.orders.domain.OrderService;
import com.sivalabs.bookstore.orders.domain.ProductServiceClient;
import com.sivalabs.bookstore.orders.mappers.OrderMapper;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
class OrderRestController {
    private static final Logger log = LoggerFactory.getLogger(OrderRestController.class);

    private final OrderService orderService;
    private final ProductServiceClient productServiceClient;

    OrderRestController(OrderService orderService, ProductServiceClient productServiceClient) {
        this.orderService = orderService;
        this.productServiceClient = productServiceClient;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CreateOrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        productServiceClient.validate(request.item().code(), request.item().price());
        OrderEntity newOrder = OrderMapper.convertToEntity(request);
        var savedOrder = orderService.createOrder(newOrder);
        return new CreateOrderResponse(savedOrder.getOrderNumber());
    }

    @GetMapping(value = "/{orderNumber}")
    OrderDto getOrder(@PathVariable String orderNumber) {
        log.info("Fetching order by orderNumber: {}", orderNumber);
        return orderService
                .findOrder(orderNumber)
                .map(OrderMapper::convertToDto)
                .orElseThrow(() -> OrderNotFoundException.forOrderNumber(orderNumber));
    }

    @GetMapping
    List<OrderView> getOrders() {
        List<OrderEntity> orders = orderService.findOrders();
        return OrderMapper.convertToOrderViews(orders);
    }
}
