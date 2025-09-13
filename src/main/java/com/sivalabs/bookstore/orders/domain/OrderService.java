package com.sivalabs.bookstore.orders.domain;

import com.sivalabs.bookstore.orders.domain.models.OrderCreatedEvent;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    OrderService(OrderRepository orderRepository, ApplicationEventPublisher publisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = publisher;
    }

    @Transactional
    public OrderEntity createOrder(OrderEntity orderEntity) {
        OrderEntity savedOrder = orderRepository.save(orderEntity);
        log.info("Created Order with orderNumber={}", savedOrder.getOrderNumber());
        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getOrderNumber(),
                savedOrder.getOrderItem().code(),
                savedOrder.getOrderItem().quantity(),
                savedOrder.getCustomer());
        eventPublisher.publishEvent(event);
        return savedOrder;
    }

    @Transactional(readOnly = true)
    public Optional<OrderEntity> findOrder(String orderNumber, Long userId) {
        return orderRepository.findByOrderNumberAndUserId(orderNumber, userId);
    }

    @Transactional(readOnly = true)
    public List<OrderEntity> findOrders(Long userId) {
        Sort sort = Sort.by("id").descending();
        return orderRepository.findAllByUserId(userId, sort);
    }
}
