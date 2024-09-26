package com.kitchen.sink.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class NotFoundException extends RuntimeException {
    private final HttpStatus status ;
    public NotFoundException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
