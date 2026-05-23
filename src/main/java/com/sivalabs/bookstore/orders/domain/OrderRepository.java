package com.sivalabs.bookstore.orders.domain;

import com.sivalabs.bookstore.orders.domain.models.OrderStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    @Query("""
        select distinct o
        from OrderEntity o left join fetch o.orderItem
        where o.userId = :userId
        """)
    List<OrderEntity> findAllByUserId(Long userId, Sort sort);

    @Query("""
        select distinct o
        from OrderEntity o left join fetch o.orderItem
        where o.orderNumber = :orderNumber and o.userId = :userId
        """)
    Optional<OrderEntity> findByOrderNumberAndUserId(String orderNumber, Long userId);

    Page<OrderEntity> findAllByStatus(OrderStatus status, Pageable pageable);

    @Query("""
        select distinct o
        from OrderEntity o left join fetch o.orderItem
        where o.orderNumber = :orderNumber
        """)
    Optional<OrderEntity> findByOrderNumber(String orderNumber);
}
