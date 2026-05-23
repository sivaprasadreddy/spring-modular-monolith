package com.sivalabs.bookstore.inventory.web;

import com.sivalabs.bookstore.inventory.domain.InvalidInventoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackages = "com.sivalabs.bookstore.inventory")
class InventoryExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(InventoryExceptionHandler.class);

    @ExceptionHandler(InvalidInventoryException.class)
    ModelAndView handle(InvalidInventoryException e) {
        log.warn("Invalid inventory operation: {}", e.getMessage());
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
