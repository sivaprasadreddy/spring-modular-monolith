package com.sivalabs.bookstore.customers.web.controllers;

import com.sivalabs.bookstore.customers.Customer;
import com.sivalabs.bookstore.customers.CustomerService;
import com.sivalabs.bookstore.customers.domain.CustomerNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
class CustomerController {
    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    CustomerController(CustomerService CustomerService) {
        this.customerService = CustomerService;
    }

    @GetMapping("/{id}")
    Customer getCustomerById(@PathVariable Long id) {
        log.info("Fetching Customer by id: {}", id);
        return customerService.getById(id).orElseThrow(() -> CustomerNotFoundException.forId(id));
    }
}
