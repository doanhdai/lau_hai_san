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
    
    // Tính tổng amount từ payments - chỉ lấy payments có payment_status = COMPLETED
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE " +
           "p.createdAt >= :startDate AND p.createdAt <= :endDate " +
           "AND p.paymentStatus = :status")
    BigDecimal calculateTotalAmountByDateAndStatus(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") PaymentStatus status);
    
    // Tính tổng amount từ payments theo ngày
    // Chỉ lấy payments có payment_status = COMPLETED (không quan trọng order status)
    // Sử dụng CAST() để tương thích với cả MySQL và SQL Server
    // Sử dụng String parameter để tương thích với cả MySQL và SQL Server
    @Query(value = "SELECT CAST(p.created_at AS DATE) as date, SUM(p.amount) as total " +
           "FROM payments p WHERE " +
           "p.created_at >= :startDate AND p.created_at <= :endDate " +
           "AND p.payment_status = :status " +
           "GROUP BY CAST(p.created_at AS DATE) " +
           "ORDER BY CAST(p.created_at AS DATE)", nativeQuery = true)
    List<Object[]> calculateAmountByDateAndStatus(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") String status);
    
    // Tính doanh thu theo ngày
    // Chỉ lấy payments có payment_status = COMPLETED (không quan trọng order status)
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE " +
           "p.createdAt >= :startDate AND p.createdAt <= :endDate " +
           "AND p.paymentStatus = 'COMPLETED'")
    BigDecimal calculateRevenueByDate(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    // Tính doanh thu theo tháng
    // Chỉ lấy payments có payment_status = COMPLETED (không quan trọng order status)
    // Sử dụng native query với MONTH() và YEAR() tương thích với cả MySQL và SQL Server
    @Query(value = "SELECT MONTH(p.created_at) as month, SUM(p.amount) as revenue " +
           "FROM payments p WHERE YEAR(p.created_at) = :year " +
           "AND p.payment_status = :status " +
           "GROUP BY MONTH(p.created_at) " +
           "ORDER BY MONTH(p.created_at)", nativeQuery = true)
    List<Object[]> calculateMonthlyRevenue(
            @Param("year") int year,
            @Param("status") String status);
}

