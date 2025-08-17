package com.eazybytes.eazystore.repository;

import com.eazybytes.eazystore.entity.Contact;
import com.eazybytes.eazystore.entity.Customer;
import com.eazybytes.eazystore.entity.Order;
import com.eazybytes.eazystore.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

  /**
   * Fetch orders for a customer, sorted by creation date in descending order.
   */
  List<Order> findByCustomerOrderByCreatedAtDesc(Customer customer);

  List<Order> findByOrderStatus(OrderStatus orderStatus);
  
  /**
   * Fetch orders for a specific customer and status, sorted by creation date in descending order.
   */
  List<Order> findByCustomerAndOrderStatusOrderByCreatedAtDesc(Customer customer, OrderStatus orderStatus);

  @Query("SELECT o FROM Order o WHERE o.customer=:customer ORDER BY o.createdAt DESC")
  List<Order> findOrdersByCustomer(@Param("customer") Customer customer);

  @Query("SELECT o FROM Order o WHERE o.orderStatus = :orderStatus")
  List<Order> findOrdersByStatus(@Param("orderStatus") OrderStatus orderStatus);

  @Query(value = "SELECT * FROM orders o WHERE o.customer_id=:customerId ORDER BY o.created_at DESC"
  , nativeQuery = true)
  List<Order> findOrdersByCustomerWithNativeQuery(@Param("customerId") Long customerId);

  // Keep native query as String since it's a direct SQL query
  @Query(value = "SELECT * FROM orders o WHERE o.order_status = :status", nativeQuery = true)
  List<Order> findOrdersByStatusWithNativeQuery(@Param("status") String status);

  @Transactional
  @Modifying
  @Query("UPDATE Order o SET o.orderStatus = :orderStatus, o.updatedAt = CURRENT_TIMESTAMP, o.updatedBy = :updatedBy WHERE o.orderId = :orderId")
  int updateOrderStatus(@Param("orderId") Long orderId, 
                      @Param("orderStatus") OrderStatus orderStatus,
                      @Param("updatedBy") String updatedBy);
}