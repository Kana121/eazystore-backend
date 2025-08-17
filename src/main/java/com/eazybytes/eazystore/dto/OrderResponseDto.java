package com.eazybytes.eazystore.dto;

import com.eazybytes.eazystore.entity.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponseDto(Long orderId, OrderStatus status,
                               BigDecimal totalPrice, String createdAt,
                               List<OrderItemReponseDto> items) {
}
