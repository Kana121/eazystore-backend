package com.eazybytes.eazystore.service.impl;

import com.eazybytes.eazystore.constants.ApplicationConstants;
import com.eazybytes.eazystore.dto.OrderItemDto;
import com.eazybytes.eazystore.dto.OrderItemReponseDto;
import com.eazybytes.eazystore.dto.OrderRequestDto;
import com.eazybytes.eazystore.dto.OrderResponseDto;
import com.eazybytes.eazystore.entity.*;
import com.eazybytes.eazystore.exception.ResourceNotFoundException;
import com.eazybytes.eazystore.repository.OrderRepository;
import com.eazybytes.eazystore.repository.ProductRepository;
import com.eazybytes.eazystore.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProfileServiceImpl profileService;
    private static final int MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private final String uploadDir = "uploads/products-images/";//
    @Override
    public void createOrder(OrderRequestDto orderRequest) {
        Customer customer = profileService.getAuthenticatedCustomer();
        // Create Order
        Order order = new Order();
        order.setCustomer(customer);
        BeanUtils.copyProperties(orderRequest, order);
        order.setOrderStatus(OrderStatus.PENDING);
        // Map OrderItems
        List<OrderItem> orderItems = orderRequest.items().stream().map(item -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "ProductID",
                            item.productId().toString()));
            orderItem.setProduct(product);
            orderItem.setQuantity(item.quantity());
            orderItem.setPrice(item.price());
            return orderItem;
        }).collect(Collectors.toList());
        order.setOrderItems(orderItems);
        orderRepository.save(order);

    }

    @Override
    public List<OrderResponseDto> getCustomerOrders() {
        Customer customer =profileService.getAuthenticatedCustomer();
        List<Order> orders = orderRepository.findOrdersByCustomerWithNativeQuery(customer.getCustomerId());

        return orders.stream().map(this::mapToOrderResponseDTO).collect(Collectors.toList());
    }
    private byte[] readImageFile(String fileName) {
        try {
            Path imagePath = Paths.get(uploadDir).resolve(fileName).normalize();
            if (!Files.exists(imagePath)) {
                throw new RuntimeException("Image file not found: " + fileName);
            }

            // Check file size before reading
            long fileSize = Files.size(imagePath);
            if (fileSize > MAX_IMAGE_SIZE) {
                throw new RuntimeException("Image file too large: " + fileName + " (max " + (MAX_IMAGE_SIZE/1024/1024) + "MB)");
            }

            return Files.readAllBytes(imagePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read image file: " + fileName, e);
        }
    }
    @Override
    public List<OrderResponseDto> getAllPendingOrders() {
        List<Order> orders = orderRepository.findByOrderStatus(OrderStatus.PENDING);
        return orders.stream()
                .map(this::mapToOrderResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDto> getOrdersByStatus(String status) {
        try {
            Customer customer = profileService.getAuthenticatedCustomer();
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            List<Order> orders = orderRepository.findByCustomerAndOrderStatusOrderByCreatedAtDesc(customer, orderStatus);
            return orders.stream()
                    .map(this::mapToOrderResponseDTO)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, String status) {
        try {
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            int updated = orderRepository.updateOrderStatus(orderId, orderStatus, currentUser);
            if (updated == 0) {
                throw new ResourceNotFoundException("Order", "OrderID", orderId.toString());
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
    }

    /**
     * Map Order entity to OrderResponseDto
     */
    private OrderResponseDto mapToOrderResponseDTO(Order order) {
        // Map Order Items
        List<OrderItemReponseDto> itemDTOs = order.getOrderItems().stream()
                .map(this::mapToOrderItemResponseDTO)
                .collect(Collectors.toList());
        
        return new OrderResponseDto(
            order.getOrderId(),
            order.getOrderStatus(),
            order.getTotalPrice(),
            order.getCreatedAt().toString(),
            itemDTOs
        );
    }

    /**
     * Map OrderItem entity to OrderItemResponseDto
     */
    @Override
    public Order createOrderWithPayment(OrderRequestDto orderRequest, String paymentId, String paymentStatus)
            throws ResourceNotFoundException {

        log.info("Creating order with payment ID: {}", paymentId);

        Customer customer = profileService.getAuthenticatedCustomer();
        Order order = new Order();
        order.setCustomer(customer);

        // Set order details from request
        order.setTotalPrice(orderRequest.totalPrice());
        order.setPaymentId(paymentId);
        order.setPaymentStatus(paymentStatus);
        order.setOrderStatus(OrderStatus.PROCESSING);

        // Map and set order items
        List<OrderItem> orderItems = orderRequest.items().stream()
                .map(item -> createOrderItem(item, order))
                .collect(Collectors.toList());

        order.setOrderItems(orderItems);

        // Save the order
        Order savedOrder = orderRepository.save(order);
        log.info("Created order {} with payment ID: {}", savedOrder.getOrderId(), paymentId);

        return savedOrder;
    }

    private OrderItem createOrderItem(OrderItemDto itemDto, Order order) {
        Product product = productRepository.findById(itemDto.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ProductID",
                        itemDto.productId().toString()));

        // Update product stock
        int newQuantity = product.getQuantity() - itemDto.quantity();
        if (newQuantity < 0) {
            throw new IllegalStateException("Insufficient stock for product: " + product.getName());
        }
        product.setQuantity(newQuantity);
        productRepository.save(product);

        // Create order item
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(itemDto.quantity());
        orderItem.setPrice(itemDto.price());

        return orderItem;
    }

    private OrderItemReponseDto mapToOrderItemResponseDTO(OrderItem orderItem) {
        List<byte[]> imageData = new ArrayList<>();

        // Get product and its image file names
        Product product = orderItem.getProduct();
        if (product.getImageFileNames() != null && !product.getImageFileNames().isEmpty()) {
            // Read each image file and add to the list
            for (String fileName : product.getImageFileNames()) {
                try {
                    byte[] imageBytes = readImageFile(fileName);
                    imageData.add(imageBytes);
                } catch (Exception e) {
                    log.warn("Failed to read image file: {}", fileName, e);
                    // Continue with other images if one fails
                }
            }
        }

        return new OrderItemReponseDto(
                product.getName(),
                orderItem.getQuantity(),
                orderItem.getPrice(),
                imageData
        );
    }
}
