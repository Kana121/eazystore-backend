package com.eazybytes.eazystore.service;

import com.eazybytes.eazystore.dto.OrderRequestDto;
import com.eazybytes.eazystore.dto.OrderResponseDto;
import com.eazybytes.eazystore.entity.Order;
import com.eazybytes.eazystore.entity.OrderStatus;
import com.eazybytes.eazystore.exception.ResourceNotFoundException;

import java.util.List;

public interface IOrderService {

    /**
     * Creates a new order with the provided order details
     * @param orderRequest The order details including items and payment information
     */
    void createOrder(OrderRequestDto orderRequest);

    /**
     * Retrieves all orders for the currently authenticated customer
     * @return List of order DTOs
     */
    List<OrderResponseDto> getCustomerOrders();

    /**
     * Retrieves all orders with a specific status
     * @param status The order status to filter by
     * @return List of order DTOs with the specified status
     * @throws IllegalArgumentException if the status is invalid
     */
    List<OrderResponseDto> getOrdersByStatus(String status) throws IllegalArgumentException;

    /**
     * Retrieves all pending orders (convenience method)
     * @return List of pending order DTOs
     */
    List<OrderResponseDto> getAllPendingOrders();

    /**
     * Updates the status of an existing order
     * @param orderId The ID of the order to update
     * @param status The new status to set (as a string, will be converted to OrderStatus)
     * @throws ResourceNotFoundException if the order is not found
     * @throws IllegalArgumentException if the status is invalid
     */
    void updateOrderStatus(Long orderId, String status) 
        throws ResourceNotFoundException, IllegalArgumentException;

    /**
     * Creates an order with payment verification
     * @param orderRequest The order details including items
     * @param paymentId The payment ID from the payment gateway
     * @param paymentStatus The status of the payment
     * @return The created order
     * @throws ResourceNotFoundException if any required resource is not found
     */
    Order createOrderWithPayment(OrderRequestDto orderRequest, String paymentId, String paymentStatus)
            throws ResourceNotFoundException;
}
