package com.eazybytes.eazystore.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class CsrfTokenController {
    
    @GetMapping("/csrf-token")
    public String getCsrfToken(HttpServletRequest request) {
        // The CsrfToken will be automatically injected by Spring Security
        return "CSRF Token: " + request.getAttribute("_csrf").toString();
    }
}
