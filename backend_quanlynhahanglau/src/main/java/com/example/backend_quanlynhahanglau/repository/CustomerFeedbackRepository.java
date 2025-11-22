package com.example.backend_quanlynhahanglau.repository;

import com.example.backend_quanlynhahanglau.entity.Customer;
import com.example.backend_quanlynhahanglau.entity.CustomerFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerFeedbackRepository extends JpaRepository<CustomerFeedback, Long> {
    List<CustomerFeedback> findByCustomer(Customer customer);
    List<CustomerFeedback> findByResolvedFalse();
    List<CustomerFeedback> findByResolvedTrue();
    
    // Lấy feedback chưa xử lý
    List<CustomerFeedback> findByResolvedFalseOrderByCreatedAtDesc();
    
    // Thống kê rating trung bình
    @Query("SELECT AVG(f.rating) FROM CustomerFeedback f")
    Double calculateAverageRating();
    
    // Lấy feedbacks công khai (có comment và rating >= 4)
    @Query("SELECT f FROM CustomerFeedback f WHERE f.comment IS NOT NULL AND f.comment != '' AND f.rating >= :rating ORDER BY f.createdAt DESC")
    List<CustomerFeedback> findPublicFeedbacks(@Param("rating") Integer rating);
    
    // Tìm feedback theo reservation ID (qua order)
    @Query("SELECT f FROM CustomerFeedback f WHERE f.order.reservation.id = :reservationId")
    List<CustomerFeedback> findByReservationId(@Param("reservationId") Long reservationId);
}
