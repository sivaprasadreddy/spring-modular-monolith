package com.sivalabs.bookstore.orders.domain;

import com.sivalabs.bookstore.orders.OrderCreatedEvent;
import com.sivalabs.bookstore.orders.OrderService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class OrderServiceImpl implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    OrderServiceImpl(OrderRepository orderRepository, ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public OrderEntity createOrder(OrderEntity newOrder) {

        OrderEntity savedOrder = this.orderRepository.save(newOrder);
        log.info("Created Order with orderNumber={}", savedOrder.getOrderNumber());
        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getOrderNumber(),
                savedOrder.getOrderItem().code(),
                savedOrder.getOrderItem().quantity(),
                savedOrder.getCustomer());
        eventPublisher.publishEvent(event);
        return savedOrder;
    }

    @Override
    public Optional<OrderEntity> findOrder(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Override
    public List<OrderView> findOrders() {
        Sort sort = Sort.by("id").descending();
        var orders = orderRepository.findAllBy(sort);
        return buildOrderViews(orders);
    }

    private List<OrderView> buildOrderViews(List<OrderEntity> orders) {
        List<OrderView> orderViews = new ArrayList<>();
        for (OrderEntity order : orders) {
            var orderView = new OrderView(order.getOrderNumber(), order.getStatus(), order.getCustomer());
            orderViews.add(orderView);
        }
        return orderViews;
    }
}
