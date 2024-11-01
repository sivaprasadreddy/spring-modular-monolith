package com.sivalabs.bookstore.services;

import com.sivalabs.bookstore.entities.OrderEntity;
import com.sivalabs.bookstore.models.CreateOrderRequest;
import com.sivalabs.bookstore.models.OrderDTO;
import com.sivalabs.bookstore.models.OrderStatus;
import java.util.UUID;

class OrderMapper {

    static OrderEntity convertToEntity(CreateOrderRequest request) {
        OrderEntity entity = new OrderEntity();
        entity.setOrderNumber(UUID.randomUUID().toString());
        entity.setStatus(OrderStatus.NEW);
        entity.setCustomer(request.customer());
        entity.setDeliveryAddress(request.deliveryAddress());
        entity.setOrderItem(request.item());
        return entity;
    }

    static OrderDTO convertToDTO(OrderEntity order) {
        return new OrderDTO(
                order.getOrderNumber(),
                order.getOrderItem(),
                order.getCustomer(),
                order.getDeliveryAddress(),
                order.getStatus(),
                order.getCreatedAt());
    }
}
