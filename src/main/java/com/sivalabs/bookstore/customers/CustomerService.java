package com.sivalabs.bookstore.customers;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CustomerService {
    Optional<Customer> getById(Long id);

    List<Customer> getByIds(Set<Long> customerIds);
}
