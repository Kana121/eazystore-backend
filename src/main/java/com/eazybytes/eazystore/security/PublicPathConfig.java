package com.eazybytes.eazystore.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PublicPathConfig {

    @Bean
    public List<String> publicPaths() {
      return List.of(
              // Auth endpoints
              "/api/v1/auth/**",
              
              // Public API endpoints
              "/api/v1/products/**",
              "/api/v1/categories/**",
              "/api/v1/admin/products/all",
              
              // Contact form
              "/api/v1/contacts/**",
              
              // CSRF token endpoint
              "/api/v1/csrf-token",
              "/csrf-token",
              
              // Error handling
              "/error",
              
              // Actuator health checks
              "/actuator/health",
              "/actuator/info",
              
              // Swagger/OpenAPI
              "/swagger-ui/**",
              "/v3/api-docs/**",
              "/swagger-ui.html"
      );
    }

}
