package com.sivalabs.bookstore.orders.domain;

import com.sivalabs.bookstore.catalog.ProductService;
import com.sivalabs.bookstore.customers.Customer;
import com.sivalabs.bookstore.customers.CustomerService;
import com.sivalabs.bookstore.orders.domain.events.OrderCreatedEvent;
import com.sivalabs.bookstore.orders.domain.models.CreateOrderRequest;
import com.sivalabs.bookstore.orders.domain.models.CreateOrderResponse;
import com.sivalabs.bookstore.orders.domain.models.CustomerDTO;
import com.sivalabs.bookstore.orders.domain.models.OrderDTO;
import com.sivalabs.bookstore.orders.domain.models.OrderSummary;
import com.sivalabs.bookstore.orders.domain.models.OrderView;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final CustomerService customerService;
    private final ApplicationEventPublisher eventPublisher;

    OrderService(
            OrderRepository orderRepository,
            ProductService productService,
            CustomerService customerService,
            ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.customerService = customerService;
        this.eventPublisher = eventPublisher;
    }

    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        validate(request);
        OrderEntity newOrder = OrderMapper.convertToEntity(request);
        OrderEntity savedOrder = this.orderRepository.save(newOrder);
        log.info("Created Order with orderNumber={}", savedOrder.getOrderNumber());
        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getOrderNumber(),
                savedOrder.getOrderItem().code(),
                savedOrder.getOrderItem().quantity(),
                savedOrder.getCustomerId());
        eventPublisher.publishEvent(event);
        return new CreateOrderResponse(savedOrder.getOrderNumber());
    }

    private void validate(CreateOrderRequest request) {
        String code = request.item().code();
        var product = productService
                .getByCode(code)
                .orElseThrow(() -> new InvalidOrderException("Product not found with code: " + code));
        if (product.price().compareTo(request.item().price()) != 0) {
            throw new InvalidOrderException("Product price mismatch");
        }
    }

    public Optional<OrderDTO> findOrder(String orderNumber) {
        Optional<OrderEntity> byOrderNumber = orderRepository.findByOrderNumber(orderNumber);
        if (byOrderNumber.isEmpty()) {
            return Optional.empty();
        }
        OrderEntity orderEntity = byOrderNumber.get();
        var customer = customerService.getById(orderEntity.getCustomerId()).orElseThrow();
        var customerDTO = new CustomerDTO(customer.id(), customer.name(), customer.email(), customer.phone());
        var orderDTO = OrderMapper.convertToDTO(orderEntity, customerDTO);
        return Optional.of(orderDTO);
    }

    public List<OrderView> findOrders() {
        Sort sort = Sort.by("id").descending();
        var orders = orderRepository.findAllBy(sort);
        Set<Long> customerIds = orders.stream().map(OrderSummary::customerId).collect(Collectors.toSet());
        List<Customer> customers = customerService.getByIds(customerIds);
        return buildOrderViews(orders, customers);
    }

    private List<OrderView> buildOrderViews(List<OrderSummary> orders, List<Customer> customers) {
        List<OrderView> orderViews = new ArrayList<>();
        for (OrderSummary order : orders) {
            Customer customer = customers.stream()
                    .filter(c -> c.id().equals(order.customerId()))
                    .findFirst()
                    .orElseThrow();
            var orderView = new OrderView(
                    order.orderNumber(),
                    order.status(),
                    new CustomerDTO(customer.id(), customer.name(), customer.email(), customer.phone()));
            orderViews.add(orderView);
        }
        return orderViews;
    }
}
