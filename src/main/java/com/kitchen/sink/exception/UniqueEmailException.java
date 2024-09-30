package com.kitchen.sink.exception;


import jakarta.validation.ValidationException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UniqueEmailException extends ValidationException {
    private final HttpStatus status;

    public UniqueEmailException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
