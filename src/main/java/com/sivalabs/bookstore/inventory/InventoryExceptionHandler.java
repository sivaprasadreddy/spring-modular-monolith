package com.sivalabs.bookstore.inventory;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class InventoryExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InvalidInventoryException.class)
    ProblemDetail handle(InvalidInventoryException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problemDetail.setTitle("Invalid Inventory Request");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
