package com.kitchen.sink.utils;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;

@Component
public class RoleResolver {

    private final List<RequestMappingHandlerMapping> handlerMappings;

    @Autowired
    public RoleResolver(List<RequestMappingHandlerMapping> handlerMappings) {
        this.handlerMappings = handlerMappings;
    }

    public List<String> getRequiredRoles(String requestURI, String requestMethod) {
        List<String> requiredRoles = new ArrayList<>();

        for (RequestMappingHandlerMapping handlerMapping : handlerMappings) {
            handlerMapping.getHandlerMethods().forEach((key, handlerMethod) -> {
                if (key.getPatternValues().stream().anyMatch(pattern -> pattern.equals(requestURI)) &&
                        key.getMethodsCondition().getMethods().stream().anyMatch(method -> method.name().equalsIgnoreCase(requestMethod))) {

                    // Extract roles from @PreAuthorize annotation
                    var preAuthorize = handlerMethod.getMethodAnnotation(org.springframework.security.access.prepost.PreAuthorize.class);

                    if (preAuthorize != null) {
                        String expression = preAuthorize.value();
                        requiredRoles.addAll(parseRoles(expression));
                    }
                }
            });
        }

        return requiredRoles;
    }

    private List<String> parseRoles(String expression) {
        // Example: hasAnyAuthority('ADMIN', 'USER') => extract 'ADMIN' and 'USER'
        String[] roles = expression.replace("hasAnyAuthority('", "").replace("')", "").split("', '");
        return List.of(roles);
    }
}
