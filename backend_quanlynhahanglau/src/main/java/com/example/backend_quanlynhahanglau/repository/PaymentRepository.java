package com.example.backend_quanlynhahanglau.repository;

import com.example.backend_quanlynhahanglau.entity.Payment;
import com.example.backend_quanlynhahanglau.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrderId(Long orderId);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE " +
           "p.createdAt >= :startDate AND p.createdAt <= :endDate " +
           "AND p.paymentStatus = :status")
    BigDecimal calculateTotalAmountByDateAndStatus(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") PaymentStatus status);
    
    // Tính tổng amount từ payments theo order_id và theo ngày
    @Query("SELECT FUNCTION('DATE', p.createdAt) as date, SUM(p.amount) as total " +
           "FROM Payment p WHERE " +
           "p.createdAt >= :startDate AND p.createdAt <= :endDate " +
           "AND p.paymentStatus = :status " +
           "GROUP BY FUNCTION('DATE', p.createdAt)")
    List<Object[]> calculateAmountByDateAndStatus(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") PaymentStatus status);
}

