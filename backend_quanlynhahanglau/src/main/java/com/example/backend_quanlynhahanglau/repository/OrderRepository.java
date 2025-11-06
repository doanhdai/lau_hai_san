package com.example.backend_quanlynhahanglau.repository;

import com.example.backend_quanlynhahanglau.entity.Customer;
import com.example.backend_quanlynhahanglau.entity.Order;
import com.example.backend_quanlynhahanglau.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByCustomer(Customer customer);
    List<Order> findByStatus(OrderStatus status);
    
    // Tìm đơn hàng theo ngày
    @Query("SELECT o FROM Order o WHERE " +
           "o.createdAt >= :startDate AND o.createdAt <= :endDate")
    List<Order> findByDate(@Param("startDate") LocalDateTime startDate, 
                           @Param("endDate") LocalDateTime endDate);
    
    // Thống kê doanh thu theo ngày (tính từ đơn đã xác nhận trở đi)
    @Query("SELECT SUM(o.total) FROM Order o WHERE " +
           "o.createdAt >= :startDate AND o.createdAt <= :endDate " +
           "AND o.status IN ('CONFIRMED', 'PREPARING', 'SERVED', 'COMPLETED')")
    BigDecimal calculateRevenueByDate(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    // Thống kê doanh thu theo tháng (tính từ đơn đã xác nhận trở đi)
    @Query("SELECT FUNCTION('MONTH', o.createdAt), SUM(o.total) FROM Order o WHERE " +
           "FUNCTION('YEAR', o.createdAt) = :year " +
           "AND o.status IN ('CONFIRMED', 'PREPARING', 'SERVED', 'COMPLETED') " +
           "GROUP BY FUNCTION('MONTH', o.createdAt)")
    List<Object[]> calculateMonthlyRevenue(@Param("year") int year);
}
