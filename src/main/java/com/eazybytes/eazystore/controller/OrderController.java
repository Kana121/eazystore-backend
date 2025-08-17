package com.eazybytes.eazystore.controller;

import com.eazybytes.eazystore.dto.OrderItemDto;
import com.eazybytes.eazystore.dto.OrderRequestDto;
import com.eazybytes.eazystore.dto.OrderResponseDto;
import com.eazybytes.eazystore.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService iOrderService;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody Map<String, Object> request) {
        try {
            log.info("Received order creation request: {}", request);
            
            // Extract items from the request
            List<Map<String, Object>> itemsList = (List<Map<String, Object>>) request.get("items");
            String orderId = (String) request.get("orderId");
            String paymentId = (String) request.get("paymentId");
            BigDecimal total = new BigDecimal(request.get("total").toString());
            
            // Convert items to OrderItemDto list
            List<OrderItemDto> orderItems = itemsList.stream()
                .map(item -> new OrderItemDto(
                    Long.parseLong(item.get("productId").toString()),
                    (Integer) item.get("quantity"),
                    new BigDecimal(item.get("price").toString())
                ))
                .collect(Collectors.toList());
            
            // Create OrderRequestDto
            OrderRequestDto orderRequest = new OrderRequestDto(
                total,
                paymentId,
                "PAID", // This is the payment status, not order status
                orderItems
            );
            
            // Create the order
            iOrderService.createOrder(orderRequest);
            log.info("Order created successfully with ID: {}", orderId);
            
            return ResponseEntity.ok("Order created successfully!");
        } catch (Exception e) {
            log.error("Error creating order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error creating order: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> loadCustomerOrders() {
        return ResponseEntity.ok(iOrderService.getCustomerOrders());
    }

}
