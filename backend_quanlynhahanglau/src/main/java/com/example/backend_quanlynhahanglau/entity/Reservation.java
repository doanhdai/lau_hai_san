package com.example.backend_quanlynhahanglau.entity;

import com.example.backend_quanlynhahanglau.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(length = 100)
    private String customerName; // Tên khách hàng từ form đặt bàn (lưu riêng cho đơn này)

    @ManyToOne
    @JoinColumn(name = "table_id")
    private RestaurantTable table;

    @Column(nullable = false)
    private LocalDateTime reservationTime; // Thời gian đặt bàn

    @Column(nullable = false)
    private Integer numberOfGuests; // Số lượng khách

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;

    @Column(length = 1000)
    private String specialRequests; // Yêu cầu đặc biệt

    @Column(length = 1000)
    private String notes;

    @Column(columnDefinition = "bit default 0")
    private Boolean emailSent = false; // Đã gửi email xác nhận

    @ManyToOne
    @JoinColumn(name = "confirmed_by")
    private User confirmedBy; // Nhân viên xác nhận

    private LocalDateTime confirmedAt;

    @Column(precision = 10, scale = 2)
    private BigDecimal depositAmount; // Số tiền đã cọc (20% tổng đơn món)

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
