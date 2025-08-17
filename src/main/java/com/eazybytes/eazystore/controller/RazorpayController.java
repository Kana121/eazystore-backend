package com.eazybytes.eazystore.controller;

import com.eazybytes.eazystore.dto.OrderItemDto;
import com.eazybytes.eazystore.dto.OrderRequestDto;
import com.eazybytes.eazystore.dto.RazorpayOrderRequest;
import com.eazybytes.eazystore.service.IRazorpayService;
import com.eazybytes.eazystore.service.IOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/payment/razorpay")
@RequiredArgsConstructor
public class RazorpayController {

    private final IRazorpayService razorpayService;
    private final IOrderService orderService;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody RazorpayOrderRequest orderRequest) {
        try {
            String order = razorpayService.createOrder(orderRequest);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error creating Razorpay order: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, Object> requestBody) {
        // Extract payment verification parameters from request body
        String orderId = (String) requestBody.get("orderId");
        String paymentId = (String) requestBody.get("paymentId");
        String signature = (String) requestBody.get("signature");
        
        log.info("Verifying payment for order: {}", orderId);
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract order details from the request
            @SuppressWarnings("unchecked")
            Map<String, Object> orderDetails = (Map<String, Object>) requestBody.get("orderDetails");
            if (orderDetails == null) {
                throw new IllegalArgumentException("Order details are required");
            }
            // Verify payment with Razorpay
            boolean isValid = razorpayService.verifyPayment(orderId, paymentId, signature);
            response.put("valid", isValid);

            if (isValid) {
                log.info("Payment verified successfully for order: {}", orderId);
                
                // Extract order items from the request
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> itemsList = (List<Map<String, Object>>) orderDetails.get("items");
                if (itemsList == null) {
                    throw new IllegalArgumentException("Order items are required");
                }
                
                BigDecimal total = new BigDecimal(orderDetails.get("amount").toString());
                
                // Convert items to OrderItemDto list
                List<OrderItemDto> orderItems = itemsList.stream()
                    .map(item -> new OrderItemDto(
                        Long.parseLong(item.get("productId").toString()),
                        (Integer) item.get("quantity"),
                        new BigDecimal(item.get("price").toString())
                    ))
                    .collect(Collectors.toList());
                
                // Create and save the order
                OrderRequestDto orderRequest = new OrderRequestDto(
                    total,
                    paymentId,
                    "PAID",
                    orderItems
                );
                
                // Use createOrderWithPayment to ensure proper order status and payment linking
                orderService.createOrderWithPayment(orderRequest, paymentId, "PAID");
                
                response.put("message", "Payment successful and order created");
                response.put("orderId", orderId);
                return ResponseEntity.ok(response);
            } else {
                log.warn("Payment verification failed for order: {}", orderId);
                response.put("message", "Payment verification failed");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error processing payment verification for order {}: {}", orderId, e.getMessage(), e);
            response.put("valid", false);
            response.put("message", "Error processing payment: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
