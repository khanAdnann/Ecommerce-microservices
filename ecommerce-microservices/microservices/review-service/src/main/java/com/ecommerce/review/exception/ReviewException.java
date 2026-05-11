package com.ecommerce.review.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ReviewException extends RuntimeException {
    
    public ReviewException(String message) {
        super(message);
    }
    
    public ReviewException(String message, Throwable cause) {
        super(message, cause);
    }
}
