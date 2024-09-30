package com.kitchen.sink.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SinkValidationException extends RuntimeException {
   private final HttpStatus status ;
    public SinkValidationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
