package com.kitchen.sink.exception;

import org.springframework.security.core.AuthenticationException;

public class UserRolesModifiedException extends AuthenticationException {
    public UserRolesModifiedException(String message) {
        super(message);
    }
}
