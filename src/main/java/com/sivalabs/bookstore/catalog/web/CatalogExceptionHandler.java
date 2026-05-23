package com.sivalabs.bookstore.catalog.web;

import com.sivalabs.bookstore.catalog.domain.DuplicateProductCodeException;
import com.sivalabs.bookstore.catalog.domain.ProductNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackages = "com.sivalabs.bookstore.catalog")
class CatalogExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(CatalogExceptionHandler.class);

    @ExceptionHandler(ProductNotFoundException.class)
    ModelAndView handle(ProductNotFoundException e) {
        log.warn("Product not found: {}", e.getMessage());
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("errorMessage", e.getMessage());
        mav.setStatus(HttpStatus.NOT_FOUND);
        return mav;
    }

    @ExceptionHandler(DuplicateProductCodeException.class)
    ModelAndView handle(DuplicateProductCodeException e) {
        log.warn("Duplicate product code: {}", e.getMessage());
        ModelAndView mav = new ModelAndView("error/400");
        mav.addObject("errorMessage", e.getMessage());
        mav.setStatus(HttpStatus.CONFLICT);
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
