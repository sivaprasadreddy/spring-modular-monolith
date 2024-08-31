package com.sivalabs.bookstore.customers.config;

import com.sivalabs.bookstore.customers.domain.CustomerNotFoundException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class CustomersExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    ProblemDetail handle(CustomerNotFoundException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problemDetail.setTitle("Customer Not Found");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
