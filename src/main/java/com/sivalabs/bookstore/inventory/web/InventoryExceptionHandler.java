package com.sivalabs.bookstore.inventory.web;

import com.sivalabs.bookstore.inventory.domain.InvalidInventoryException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
class InventoryExceptionHandler {

    @ExceptionHandler(InvalidInventoryException.class)
    ModelAndView handle(InvalidInventoryException e) {
        ModelAndView mav = new ModelAndView("error/400");
        mav.addObject("errorMessage", e.getMessage());
        mav.setStatus(HttpStatus.BAD_REQUEST);
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
