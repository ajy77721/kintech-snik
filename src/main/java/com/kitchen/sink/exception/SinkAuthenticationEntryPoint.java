package com.kitchen.sink.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kitchen.sink.dto.APIResponseDTO;
import com.kitchen.sink.dto.ErrorDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class SinkAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Autowired
    private ObjectMapper objectMapper;
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        {
            APIResponseDTO<ErrorDTO> apiResponse;

            if (authException instanceof InsufficientAuthenticationException
            ) {
                apiResponse = APIResponseDTO.<ErrorDTO>builder()
                        .status(false)
                        .error(ErrorDTO.builder()
                                .message(authException.getMessage()).build()).build();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
            } else {
                String message = getMessage(authException);
                apiResponse = APIResponseDTO.<ErrorDTO>builder()
                        .status(false)
                        .error(ErrorDTO.builder()
                                .message(message).build()).build();
                response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
            }

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));

        }
    }

    private static String getMessage(AuthenticationException authException) {
        String message;
        if (authException instanceof LockedException) {
            message = "{\"error\": \"Your account is locked.\"}";
        } else if (authException instanceof DisabledException) {
            message = "{\"error\": \"Your account is disabled.\"}";
        } else if (authException instanceof AccountExpiredException) {
            message = "{\"error\": \"Your account has expired.\"}";
        } else if (authException.getMessage().equalsIgnoreCase("Bad credentials")) {
            message = "{\"error\": \"Invalid username or password.\"}";
        } else {
            message = "{\"error\": \"Authentication failed: " + authException.getMessage() + "\"}";
        }
        return message;
    }
}
