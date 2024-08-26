package com.sivalabs.bookstore.customers.domain;

import com.sivalabs.bookstore.customers.Customer;
import com.sivalabs.bookstore.customers.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository repo;

    CustomerServiceImpl(CustomerRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<Customer> getById(Long id) {
        return repo.findByCustomerId(id);
    }

    @Override
    public List<Customer> getByIds(Set<Long> customerIds) {
        return repo.findByCustomerIds(customerIds);
    }
}
