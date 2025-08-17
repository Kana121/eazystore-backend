package com.eazybytes.eazystore.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class CsrfTokenController {
    
    @GetMapping("/csrf-token")
    public String getCsrfToken(HttpServletRequest request) {
        // Get the CSRF token from the request attributes
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        
        if (csrfToken != null) {
            // Return just the token value
            return csrfToken.getToken();
        }
        
        return "CSRF token not found";
    }
}
