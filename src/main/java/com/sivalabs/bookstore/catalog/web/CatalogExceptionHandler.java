package com.sivalabs.bookstore.catalog.web;

import com.sivalabs.bookstore.catalog.domain.DuplicateProductCodeException;
import com.sivalabs.bookstore.catalog.domain.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
class CatalogExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    ModelAndView handle(ProductNotFoundException e) {
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("errorMessage", e.getMessage());
        mav.setStatus(HttpStatus.NOT_FOUND);
        return mav;
    }

    @ExceptionHandler(DuplicateProductCodeException.class)
    ModelAndView handle(DuplicateProductCodeException e) {
        ModelAndView mav = new ModelAndView("error/400");
        mav.addObject("errorMessage", e.getMessage());
        mav.setStatus(HttpStatus.CONFLICT);
        return mav;
    }

    @ExceptionHandler(Exception.class)
    ModelAndView handle(Exception e) {
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("errorMessage", e.getMessage());
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return mav;
    }
}
