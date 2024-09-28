package com.kitchen.sink.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kitchen.sink.dto.APIResponseDTO;
import com.kitchen.sink.dto.ErrorDTO;
import com.kitchen.sink.utils.RoleResolver;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SinkAccessDeniedHandler implements AccessDeniedHandler {

    @Autowired
    private RoleResolver roleResolver;
    @Autowired
    private ObjectMapper   objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Authentication authentication = (Authentication) request.getUserPrincipal();
        List<String> userRoles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String requestedUrl = request.getRequestURI();
        String requestMethod = request.getMethod();

        // Get required roles for the requested URL and method
        List<String> requiredRoles = roleResolver.getRequiredRoles(requestedUrl, requestMethod);

        String message = String.format("{\"error\": \"Access denied to URL: %s. You have roles: %s. Required roles: %s.\"}",
                requestedUrl,
                userRoles,
                requiredRoles);
      APIResponseDTO<?>  apiResponse = APIResponseDTO.<ErrorDTO>builder()
                .status(false)
                .error(ErrorDTO.builder()
                        .message(message).build()).build();

        // Set the response properties
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
