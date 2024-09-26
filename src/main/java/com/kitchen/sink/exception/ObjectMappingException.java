package com.kitchen.sink.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ObjectMappingException extends RuntimeException {
    public ObjectMappingException(String message) {
        super(message);
    }
}
