package com.sivalabs.bookstore.orders.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    @Query("""
        select distinct o
        from OrderEntity o left join fetch o.orderItem
        """)
    List<OrderEntity> findAllBy(Sort sort);

    @Query(
            """
        select distinct o
        from OrderEntity o left join fetch o.orderItem
        where o.orderNumber = :orderNumber
        """)
    Optional<OrderEntity> findByOrderNumber(String orderNumber);
}
