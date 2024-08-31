package com.sivalabs.bookstore.orders.domain;

import com.sivalabs.bookstore.orders.domain.models.OrderSummary;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    @Query(
            """
        select new com.sivalabs.bookstore.orders.domain.models.OrderSummary(
        o.orderNumber, o.customerId, o.status)
        from OrderEntity o
        """)
    List<OrderSummary> findAllBy(Sort sort);

    @Query(
            """
        select distinct o
        from OrderEntity o left join fetch o.orderItem
        where o.orderNumber = :orderNumber
        """)
    Optional<OrderEntity> findByOrderNumber(String orderNumber);
}
