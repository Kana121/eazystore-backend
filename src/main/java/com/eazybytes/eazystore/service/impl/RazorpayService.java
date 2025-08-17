package com.eazybytes.eazystore.service.impl;

import com.eazybytes.eazystore.service.IRazorpayService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.eazybytes.eazystore.dto.RazorpayOrderRequest;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class RazorpayService implements IRazorpayService {

    private final RazorpayClient razorpayClient;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Override
    public String createOrder(RazorpayOrderRequest orderRequest) throws RazorpayException {
        try {
            JSONObject orderRequestJson = new JSONObject();
            orderRequestJson.put("amount", orderRequest.getAmount().multiply(new BigDecimal(100)).intValue());
            orderRequestJson.put("currency", orderRequest.getCurrency());
            orderRequestJson.put("receipt", orderRequest.getReceipt());
//            orderRequestJson.put("payment_capture", orderRequest.getPaymentCapture());

            log.info("Creating Razorpay order for amount: {} {}",
                    orderRequest.getAmount(), orderRequest.getCurrency());

            Order order = razorpayClient.orders.create(orderRequestJson);
            log.info("Razorpay order created successfully: {}"+ order.get("id"));

            return order.toString();
        } catch (RazorpayException e) {
            log.error("Error creating Razorpay order: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean verifyPayment(String orderId, String paymentId, String signature) {
        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", orderId);
            attributes.put("razorpay_payment_id", paymentId);
            attributes.put("razorpay_signature", signature);

            // Use the secret key from properties for verification
            boolean isValid = Utils.verifyPaymentSignature(attributes, razorpayKeySecret);
            return isValid;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}