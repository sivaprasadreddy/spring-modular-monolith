package com.sivalabs.bookstore.orders.web;

import com.sivalabs.bookstore.orders.domain.InvalidOrderException;
import com.sivalabs.bookstore.orders.domain.OrderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackages = "com.sivalabs.bookstore.orders")
class OrdersExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(OrdersExceptionHandler.class);

    @ExceptionHandler(OrderNotFoundException.class)
    ModelAndView handle(OrderNotFoundException e) {
        log.warn("Order not found: {}", e.getMessage());
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("errorMessage", e.getMessage());
        mav.setStatus(HttpStatus.NOT_FOUND);
        return mav;
    }

    @ExceptionHandler(InvalidOrderException.class)
    ModelAndView handle(InvalidOrderException e) {
        log.warn("Invalid order: {}", e.getMessage());
        ModelAndView mav = new ModelAndView("error/400");
        mav.addObject("errorMessage", e.getMessage());
        mav.setStatus(HttpStatus.BAD_REQUEST);
        return mav;
    }

    @ExceptionHandler(Exception.class)
    ModelAndView handle(Exception e) {
        log.error("Unexpected error", e);
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("errorMessage", e.getMessage());
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return mav;
    }
}
