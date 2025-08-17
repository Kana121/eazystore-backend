package com.eazybytes.eazystore.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RazorpayOrderRequest {
    private BigDecimal amount;
    private String currency;
    private String receipt;
//    private Integer paymentCapture;
}
