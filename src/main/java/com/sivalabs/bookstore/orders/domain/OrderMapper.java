package com.sivalabs.bookstore.orders.domain;

import com.sivalabs.bookstore.orders.domain.models.CreateOrderCmd;
import com.sivalabs.bookstore.orders.domain.models.OrderDto;
import com.sivalabs.bookstore.orders.domain.models.OrderStatus;
import com.sivalabs.bookstore.orders.domain.models.OrderView;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class OrderMapper {
    private OrderMapper() {}

    public static OrderEntity convertToEntity(CreateOrderCmd cmd) {
        OrderEntity entity = new OrderEntity();
        entity.setOrderNumber(UUID.randomUUID().toString());
        if (cmd.userId() != null) {
            entity.setUserId(cmd.userId().getUserId());
        }
        entity.setStatus(OrderStatus.NEW);
        entity.setCustomer(cmd.customer());
        entity.setDeliveryAddress(cmd.deliveryAddress());
        entity.setOrderItem(cmd.item());
        return entity;
    }

    public static OrderDto convertToDto(OrderEntity order) {
        return new OrderDto(
                order.getOrderNumber(),
                order.getUserId(),
                order.getOrderItem(),
                order.getCustomer(),
                order.getDeliveryAddress(),
                order.getStatus(),
                order.getCreatedAt());
    }

    public static List<OrderView> convertToOrderViews(List<OrderEntity> orders) {
        List<OrderView> orderViews = new ArrayList<>();
        for (OrderEntity order : orders) {
            var orderView = new OrderView(order.getOrderNumber(), order.getStatus(), order.getCustomer());
            orderViews.add(orderView);
        }
        return orderViews;
    }
}
