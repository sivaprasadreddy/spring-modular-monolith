package com.sivalabs.bookstore.orders.domain;

import com.sivalabs.bookstore.orders.cache.OrderCacheService;
import com.sivalabs.bookstore.orders.domain.models.OrderCreatedEvent;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderCacheService orderCacheService;

    OrderService(
            OrderRepository orderRepository,
            ApplicationEventPublisher publisher,
            @Autowired(required = false) OrderCacheService orderCacheService) {
        this.orderRepository = orderRepository;
        this.eventPublisher = publisher;
        this.orderCacheService = orderCacheService;

        if (orderCacheService != null) {
            log.info("OrderService initialized with cache support enabled");
        } else {
            log.info("OrderService initialized without cache support (cache disabled or unavailable)");
        }
    }

    /**
     * Check if cache service is available and operational.
     *
     * @return true if cache service is available, false otherwise
     */
    private boolean isCacheAvailable() {
        return orderCacheService != null && !orderCacheService.isCircuitBreakerOpen();
    }

    @Transactional
    public OrderEntity createOrder(OrderEntity orderEntity) {
        OrderEntity savedOrder = orderRepository.save(orderEntity);
        log.info("Created Order with orderNumber={}", savedOrder.getOrderNumber());

        // Cache the newly created order if cache service is available
        if (isCacheAvailable()) {
            try {
                orderCacheService.cacheOrder(savedOrder.getOrderNumber(), savedOrder);
                log.debug("Order cached successfully: {}", savedOrder.getOrderNumber());
            } catch (Exception e) {
                log.warn(
                        "Failed to cache order {} - order creation will continue: {}",
                        savedOrder.getOrderNumber(),
                        e.getMessage());
                // Continue with order creation - cache failure should not break business logic
            }
        } else {
            log.debug(
                    "Cache service unavailable - skipping cache operation for order: {}", savedOrder.getOrderNumber());
        }

        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getOrderNumber(),
                savedOrder.getOrderItem().code(),
                savedOrder.getOrderItem().quantity(),
                savedOrder.getCustomer());
        eventPublisher.publishEvent(event);
        return savedOrder;
    }

    @Transactional(readOnly = true)
    public Optional<OrderEntity> findOrder(String orderNumber) {
        // Try cache first if available
        if (isCacheAvailable()) {
            try {
                Optional<OrderEntity> cachedOrder = orderCacheService.findByOrderNumber(orderNumber);
                if (cachedOrder.isPresent()) {
                    log.debug("Order found in cache: {}", orderNumber);
                    return cachedOrder;
                }
                log.debug("Cache miss for order: {}", orderNumber);
            } catch (Exception e) {
                log.warn(
                        "Failed to read from cache for order {} - falling back to database: {}",
                        orderNumber,
                        e.getMessage());
            }
        } else {
            log.debug("Cache service unavailable - querying database directly for order: {}", orderNumber);
        }

        // Cache miss or cache unavailable - query database
        Optional<OrderEntity> order = orderRepository.findByOrderNumber(orderNumber);

        // Cache the result if found and cache is available
        if (order.isPresent() && isCacheAvailable()) {
            try {
                orderCacheService.cacheOrder(orderNumber, order.get());
                log.debug("Order cached after database retrieval: {}", orderNumber);
            } catch (Exception e) {
                log.warn("Failed to cache order {} after database retrieval: {}", orderNumber, e.getMessage());
                // Continue with returning the order - cache failure should not affect read operation
            }
        }

        return order;
    }

    @Transactional(readOnly = true)
    public List<OrderEntity> findOrders() {
        Sort sort = Sort.by("id").descending();
        return orderRepository.findAllBy(sort);
    }
}
