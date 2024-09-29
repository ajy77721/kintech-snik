package com.kitchen.sink.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RoleResolver {

    private final List<RequestMappingHandlerMapping> handlerMappings;

    @Autowired
    public RoleResolver(List<RequestMappingHandlerMapping> handlerMappings) {
        this.handlerMappings = handlerMappings;
    }

    public List<String> getRequiredRoles(String requestURI, String requestMethod) {
        log.info("Getting required roles for URI: {} and Method: {}", requestURI, requestMethod);
        List<String> requiredRoles = new ArrayList<>();

        for (RequestMappingHandlerMapping handlerMapping : handlerMappings) {
            handlerMapping.getHandlerMethods().forEach((key, handlerMethod) -> {
                if (key.getPatternValues().stream().anyMatch(pattern -> pattern.equals(requestURI)) &&
                        key.getMethodsCondition().getMethods().stream().anyMatch(method -> method.name().equalsIgnoreCase(requestMethod))) {

                    log.debug("Matching handler found for URI: {} and Method: {}", requestURI, requestMethod);

                    // Extract roles from @PreAuthorize annotation
                    var preAuthorize = handlerMethod.getMethodAnnotation(org.springframework.security.access.prepost.PreAuthorize.class);

                    if (preAuthorize != null) {
                        String expression = preAuthorize.value();
                        log.debug("Found @PreAuthorize annotation with expression: {}", expression);
                        requiredRoles.addAll(parseRoles(expression));
                    }
                }
            });
        }

        log.info("Required roles for URI: {} and Method: {} are {}", requestURI, requestMethod, requiredRoles);
        return requiredRoles;
    }

    private List<String> parseRoles(String expression) {
        log.debug("Parsing roles from expression: {}", expression);
        // Example: hasAnyAuthority('ADMIN', 'USER') => extract 'ADMIN' and 'USER'
        String[] roles = expression.replace("hasAnyAuthority('", "").replace("')", "").split("', '");
        List<String> parsedRoles = List.of(roles);
        log.debug("Parsed roles: {}", parsedRoles);
        return parsedRoles;
    }
}