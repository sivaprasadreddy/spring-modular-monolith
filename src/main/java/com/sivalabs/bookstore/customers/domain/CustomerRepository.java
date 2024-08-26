package com.sivalabs.bookstore.customers.domain;

import com.sivalabs.bookstore.customers.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    @Query("""
        select new com.sivalabs.bookstore.customers.Customer(
                c.id, c.name, c.email, c.phone)
        from CustomerEntity c
        where c.id = :id
        """)
    Optional<Customer> findByCustomerId(Long id);

    @Query("""
        select new com.sivalabs.bookstore.customers.Customer(
                c.id, c.name, c.email, c.phone)
        from CustomerEntity c
        where c.id in (:customerIds)
        """)
    List<Customer> findByCustomerIds(Set<Long> customerIds);
}
