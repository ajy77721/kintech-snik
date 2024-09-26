package com.kitchen.sink.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@ConfigurationProperties(prefix = "cors")
@Configuration
@Data
public class CorsConfig {
    private List<String> allowedOrigins = List.of("*");
    private List<String> allowedMethods = List.of("*");
    private List<String> allowedHeaders= List.of("*");
    private List<String> exposedHeaders= List.of("*");
    private boolean allowCredentials    = true;
    private String urlPattern        = "/**";
}
