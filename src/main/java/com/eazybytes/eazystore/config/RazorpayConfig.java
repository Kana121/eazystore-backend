package com.eazybytes.eazystore.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RazorpayConfig {

    @Value("${razorpay.key.id:}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:}")
    private String razorpayKeySecret;

    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        log.info("Initializing Razorpay client with key ID: {}", 
            razorpayKeyId != null && !razorpayKeyId.isEmpty() ? "[SET]" : "[NOT SET]");
        
        if (razorpayKeyId == null || razorpayKeyId.isEmpty() || 
            razorpayKeySecret == null || razorpayKeySecret.isEmpty()) {
            throw new IllegalStateException("Razorpay key ID and secret must be configured");
        }
        
        return new RazorpayClient(razorpayKeyId, razorpayKeySecret);
    }
}