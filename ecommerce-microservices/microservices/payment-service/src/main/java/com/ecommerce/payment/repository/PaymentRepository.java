package com.ecommerce.payment.repository;

import com.ecommerce.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByPaymentReference(String paymentReference);
    
    boolean existsByPaymentReference(String paymentReference);
    
    Page<Payment> findByUserId(Long userId, Pageable pageable);
    
    Page<Payment> findByOrderId(Long orderId, Pageable pageable);
    
    Page<Payment> findByStatus(Payment.PaymentStatus status, Pageable pageable);
    
    Page<Payment> findByPaymentMethod(Payment.PaymentMethod paymentMethod, Pageable pageable);
    
    @Query("SELECT p FROM Payment p WHERE (:userId IS NULL OR p.userId = :userId) " +
           "AND (:orderId IS NULL OR p.orderId = :orderId) " +
           "AND (:status IS NULL OR p.status = :status) " +
           "AND (:paymentMethod IS NULL OR p.paymentMethod = :paymentMethod) " +
           "AND (:fromDate IS NULL OR p.createdAt >= :fromDate) " +
           "AND (:toDate IS NULL OR p.createdAt <= :toDate)")
    Page<Payment> searchPayments(@Param("userId") Long userId,
                               @Param("orderId") Long orderId,
                               @Param("status") Payment.PaymentStatus status,
                               @Param("paymentMethod") Payment.PaymentMethod paymentMethod,
                               @Param("fromDate") LocalDateTime fromDate,
                               @Param("toDate") LocalDateTime toDate,
                               Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    long countByStatus(@Param("status") Payment.PaymentStatus status);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.userId = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.createdAt >= :startDate AND p.createdAt <= :endDate")
    long countPaymentsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") Payment.PaymentStatus status);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.createdAt >= :startDate AND p.createdAt <= :endDate AND p.status = 'COMPLETED'")
    BigDecimal sumRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT p FROM Payment p WHERE p.status = 'COMPLETED' AND p.gatewayTransactionId IS NOT NULL")
    List<Payment> findCompletedPaymentsWithGatewayTransactionId();
    
    @Query("SELECT p FROM Payment p WHERE p.status = :status ORDER BY p.createdAt DESC")
    Page<Payment> findByStatusOrderByCreatedAtDesc(@Param("status") Payment.PaymentStatus status, Pageable pageable);
}
