package com.eazybytes.eazystore.service;

import com.eazybytes.eazystore.dto.RazorpayOrderRequest;
import com.razorpay.RazorpayException;

public interface IRazorpayService {
    String createOrder(RazorpayOrderRequest orderRequest) throws RazorpayException;

    boolean verifyPayment(String orderId, String paymentId, String signature);
}
