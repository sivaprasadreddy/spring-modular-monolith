package com.sivalabs.bookstore.orders.domain;

import com.sivalabs.bookstore.orders.domain.models.CreateOrderRequest;
import com.sivalabs.bookstore.orders.domain.models.CustomerDTO;
import com.sivalabs.bookstore.orders.domain.models.OrderDTO;
import com.sivalabs.bookstore.orders.domain.models.OrderStatus;

import java.util.UUID;

class OrderMapper {

    static OrderEntity convertToEntity(CreateOrderRequest request) {
        OrderEntity entity = new OrderEntity();
        entity.setOrderNumber(UUID.randomUUID().toString());
        entity.setStatus(OrderStatus.NEW);
        entity.setCustomerId(request.customerId());
        entity.setDeliveryAddress(request.deliveryAddress());
        entity.setOrderItem(request.item());
        return entity;
    }

    static OrderDTO convertToDTO(OrderEntity order, CustomerDTO customerDTO) {
        return new OrderDTO(
                order.getOrderNumber(),
                order.getOrderItem(),
                customerDTO,
                order.getDeliveryAddress(),
                order.getStatus(),
                order.getCreatedAt());
    }
}
