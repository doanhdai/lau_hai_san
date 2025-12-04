package com.example.backend_quanlynhahanglau.repository;

import com.example.backend_quanlynhahanglau.entity.Customer;
import com.example.backend_quanlynhahanglau.entity.Order;
import com.example.backend_quanlynhahanglau.entity.RestaurantTable;
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
    List<Order> findByTable(RestaurantTable table);
    
    // Tìm đơn hàng theo tableId và fetch orderDetails để tránh lazy loading
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderDetails od " +
           "LEFT JOIN FETCH od.dish " +
           "LEFT JOIN FETCH o.table " +
           "LEFT JOIN FETCH o.customer " +
           "LEFT JOIN FETCH o.reservation " +
           "WHERE o.table.id = :tableId")
    List<Order> findByTableId(@Param("tableId") Long tableId);
    
    // Tìm đơn hàng theo reservation ID và fetch Dish để tránh lazy loading
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderDetails od " +
           "LEFT JOIN FETCH od.dish " +
           "WHERE o.reservation.id = :reservationId")
    List<Order> findByReservationId(@Param("reservationId") Long reservationId);
    
    // Tìm đơn hàng theo ngày và fetch table để tránh lazy loading
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.table " +
           "LEFT JOIN FETCH o.customer " +
           "LEFT JOIN FETCH o.reservation " +
           "WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate")
    List<Order> findByDate(@Param("startDate") LocalDateTime startDate, 
                           @Param("endDate") LocalDateTime endDate);
    
    // Tìm tất cả đơn hàng và fetch table, customer, reservation, orderDetails để tránh lazy loading
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.table " +
           "LEFT JOIN FETCH o.customer " +
           "LEFT JOIN FETCH o.reservation " +
           "LEFT JOIN FETCH o.orderDetails od " +
           "LEFT JOIN FETCH od.dish")
    List<Order> findAllWithRelations();
    
    // Tìm đơn hàng theo ID và fetch đầy đủ relations
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.table " +
           "LEFT JOIN FETCH o.customer " +
           "LEFT JOIN FETCH o.reservation " +
           "LEFT JOIN FETCH o.orderDetails od " +
           "LEFT JOIN FETCH od.dish " +
           "WHERE o.id = :id")
    Optional<Order> findByIdWithRelations(@Param("id") Long id);
    
    // Thống kê doanh thu theo ngày (tính từ đơn đã xác nhận trở đi)
    @Query("SELECT SUM(o.total) FROM Order o WHERE " +
           "o.createdAt >= :startDate AND o.createdAt <= :endDate " +
           "AND o.status IN ('CONFIRMED', 'PREPARING', 'SERVED', 'COMPLETED')")
    BigDecimal calculateRevenueByDate(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    // Thống kê doanh thu theo tháng (tính từ đơn đã xác nhận trở đi)
    // Sử dụng native query với MONTH() và YEAR() tương thích với cả MySQL và SQL Server
    @Query(value = "SELECT MONTH(o.created_at) as month, SUM(o.total) as revenue " +
           "FROM orders o WHERE YEAR(o.created_at) = :year " +
           "AND o.status IN ('CONFIRMED', 'PREPARING', 'SERVED', 'COMPLETED') " +
           "GROUP BY MONTH(o.created_at) " +
           "ORDER BY MONTH(o.created_at)", nativeQuery = true)
    List<Object[]> calculateMonthlyRevenue(@Param("year") int year);
}

// package com.example.backend_quanlynhahanglau.repository;

// import com.example.backend_quanlynhahanglau.entity.Customer;
// import com.example.backend_quanlynhahanglau.entity.Order;
// import com.example.backend_quanlynhahanglau.enums.OrderStatus;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;

// import java.math.BigDecimal;
// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Optional;

// @Repository
// public interface OrderRepository extends JpaRepository<Order, Long> {
//     Optional<Order> findByOrderNumber(String orderNumber);
//     List<Order> findByCustomer(Customer customer);
//     List<Order> findByStatus(OrderStatus status);
    
//     // Tìm đơn hàng theo ngày
//     @Query("SELECT o FROM Order o WHERE " +
//            "o.createdAt >= :startDate AND o.createdAt <= :endDate")
//     List<Order> findByDate(@Param("startDate") LocalDateTime startDate, 
//                            @Param("endDate") LocalDateTime endDate);
    
//     // Thống kê doanh thu theo ngày (tính từ đơn đã xác nhận trở đi)
//     @Query("SELECT SUM(o.total) FROM Order o WHERE " +
//            "o.createdAt >= :startDate AND o.createdAt <= :endDate " +
//            "AND o.status IN ('CONFIRMED', 'PREPARING', 'SERVED', 'COMPLETED')")
//     BigDecimal calculateRevenueByDate(@Param("startDate") LocalDateTime startDate, 
//                                       @Param("endDate") LocalDateTime endDate);
    
//     // Thống kê doanh thu theo tháng (tính từ đơn đã xác nhận trở đi)
//     // Sử dụng MONTH() và YEAR() tương thích với cả MySQL và SQL Server
//     @Query(value = "SELECT MONTH(o.created_at) as month, SUM(o.total) as revenue " +
//            "FROM orders o WHERE YEAR(o.created_at) = :year " +
//            "AND o.status IN ('CONFIRMED', 'PREPARING', 'SERVED', 'COMPLETED') " +
//            "GROUP BY MONTH(o.created_at)", nativeQuery = true)
//     List<Object[]> calculateMonthlyRevenue(@Param("year") int year);
// }
