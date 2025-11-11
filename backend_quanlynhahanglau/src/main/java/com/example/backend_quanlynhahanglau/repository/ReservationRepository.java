package com.example.backend_quanlynhahanglau.repository;

import com.example.backend_quanlynhahanglau.entity.Customer;
import com.example.backend_quanlynhahanglau.entity.Reservation;
import com.example.backend_quanlynhahanglau.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByCustomer(Customer customer);
    List<Reservation> findByStatus(ReservationStatus status);
    
    // Tìm đặt bàn theo ngày
    @Query("SELECT r FROM Reservation r WHERE " +
           "r.reservationTime >= :startDate AND r.reservationTime <= :endDate")
    List<Reservation> findByDate(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
    
    // Tìm đặt bàn theo tên khách hàng
    @Query("SELECT r FROM Reservation r WHERE " +
           "LOWER(r.customer.fullName) LIKE LOWER(CONCAT('%', :customerName, '%'))")
    List<Reservation> searchByCustomerName(@Param("customerName") String customerName);
    
    // Lịch sử đặt bàn của khách hàng
    List<Reservation> findByCustomerOrderByCreatedAtDesc(Customer customer);
    List<Reservation> findByCustomerIdOrderByReservationTimeDesc(Long customerId);
    
    // Thống kê đặt bàn theo trạng thái
    @Query("SELECT r.status, COUNT(r) FROM Reservation r GROUP BY r.status")
    List<Object[]> countByStatus();
    
    // Đặt bàn chưa xác nhận
    List<Reservation> findByStatusAndEmailSentFalse(ReservationStatus status);
    
    // Tìm đặt bàn của một bàn trong khoảng thời gian conflict (±3 giờ)
    // Dùng để kiểm tra xem bàn có được đặt trong khoảng thời gian này không
    @Query("SELECT r FROM Reservation r WHERE r.table.id = :tableId " +
           "AND r.status IN ('PENDING', 'CONFIRMED') " +
           "AND r.reservationTime >= :startTime AND r.reservationTime <= :endTime")
    List<Reservation> findConflictingReservationsByTable(
            @Param("tableId") Long tableId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    // Tìm lịch sử đặt bàn theo phone hoặc email của Customer (liên kết với User)
    @Query("SELECT r FROM Reservation r WHERE " +
           "(:phone IS NOT NULL AND :phone != '' AND r.customer.phone = :phone) OR " +
           "(:email IS NOT NULL AND :email != '' AND r.customer.email = :email) " +
           "ORDER BY r.reservationTime DESC")
    List<Reservation> findByCustomerPhoneOrEmailOrderByReservationTimeDesc(
            @Param("phone") String phone,
            @Param("email") String email);
}
