package com.eazybytes.eazystore.security;

import com.eazybytes.eazystore.filter.JWTTokenValidatorFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class EazyStoreSecurityConfig {

    private final List<String> publicPaths;

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        // Configure CSRF with CookieCsrfTokenRepository
        http.csrf(csrf -> {
            CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
            // Match frontend expectations
            tokenRepository.setHeaderName("X-XSRF-TOKEN");  // Header name for the token
            tokenRepository.setParameterName("_csrf");      // Default parameter name
            tokenRepository.setCookieName("XSRF-TOKEN");    // Cookie name that frontend reads from
            tokenRepository.setCookieHttpOnly(false);       // Allow JavaScript to read the cookie
            
            // Disable CSRF for public endpoints
            csrf.ignoringRequestMatchers(
                "/csrf-token",
                "/api/v1/csrf-token",
                "/api/v1/auth/**",
                "/api/v1/contacts/**",
                "/api/v1/admin/products/all",
                "/actuator/health",
                "/actuator/info",
                "/swagger-ui/**",
                "/v3/api-docs/**"
            );
            
            csrf.csrfTokenRepository(tokenRepository);
            csrf.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler());
        });

        // Enable CORS
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // Configure authorization
        http.authorizeHttpRequests(auth -> {
            // Public endpoints
            publicPaths.forEach(path -> auth.requestMatchers(path).permitAll());
            
            // Explicitly permit CSRF token endpoint
            auth.requestMatchers("/csrf-token").permitAll();
            
            // Admin endpoints
            auth.requestMatchers("/api/v1/admin/**").hasRole("ADMIN");
            
            // Actuator endpoints
            auth.requestMatchers("/actuator/health").permitAll();
            auth.requestMatchers("/actuator/info").permitAll();
            auth.requestMatchers("/eazystore/actuator/**").hasRole("OPS_ENG");
            
            // Swagger/OpenAPI
            auth.requestMatchers(
                "/swagger-ui.html", 
                "/swagger-ui/**", 
                "/v3/api-docs/**"
            ).permitAll();
            
            // All other API endpoints require authentication
            auth.anyRequest().authenticated();
        });

        // Add JWT filter
        http.addFilterBefore(new JWTTokenValidatorFilter(publicPaths), BasicAuthenticationFilter.class);

        // Disable form login and basic auth for API
        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());
        
        // Disable session creation
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Handle unauthorized requests
        http.exceptionHandling(exception -> 
            exception.authenticationEntryPoint((request, response, authException) -> 
                response.sendError(401, "Unauthorized")
            )
        );

        return http.build();
    }


    @Bean
    public AuthenticationManager authenticationManager(
             AuthenticationProvider authenticationProvider) {
        var providerManager = new ProviderManager(authenticationProvider);
        return providerManager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CompromisedPasswordChecker compromisedPasswordChecker() {
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Add default origins for development
        List<String> allowedOrigins = new ArrayList<>(Arrays.asList(
            "http://localhost:3000",
            "https://eazystore-frontend.onrender.com"
        ));
        
        // Add custom frontend URLs from configuration if provided
        if (frontendUrl != null && !frontendUrl.trim().isEmpty()) {
            // Handle multiple frontend URLs if provided as comma-separated
            String[] urls = frontendUrl.split(",");
            for (String url : urls) {
                String trimmedUrl = url.trim();
                if (!trimmedUrl.isEmpty() && !allowedOrigins.contains(trimmedUrl)) {
                    allowedOrigins.add(trimmedUrl);
                }
            }
        }
        
        // Set allowed origins
        config.setAllowedOrigins(allowedOrigins);
        
        // Allow all HTTP methods
        config.setAllowedMethods(Arrays.asList(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.DELETE.name(),
            HttpMethod.PATCH.name(),
            HttpMethod.OPTIONS.name(),
            HttpMethod.HEAD.name()
        ));
        
        // Allow all headers including custom headers
        config.setAllowedHeaders(Arrays.asList(
            "*",
            "X-Requested-With",
            "Origin",
            "Content-Type",
            "Accept",
            "Authorization",
            "X-XSRF-TOKEN"
        ));
        
        // Allow credentials
        config.setAllowCredentials(true);
        
        // Expose necessary headers to the client
        config.setExposedHeaders(Arrays.asList(
            "X-XSRF-TOKEN",
            "Authorization",
            "Content-Type",
            "Content-Disposition"
        ));
        
        // Set max age for preflight requests (1 hour)
        config.setMaxAge(3600L);
        
        // Apply CORS configuration to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return source;
    }

}
