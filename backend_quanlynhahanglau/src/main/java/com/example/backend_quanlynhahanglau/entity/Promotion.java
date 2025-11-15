package com.example.backend_quanlynhahanglau.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(precision = 5, scale = 2)
    private BigDecimal discountPercent; // Phần trăm giảm giá

    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount; // Số tiền giảm giá cố định

    @Column(precision = 10, scale = 2)
    private BigDecimal minOrderValue; // Giá trị đơn hàng tối thiểu

    @Column(precision = 10, scale = 2)
    private BigDecimal maxDiscount; // Giảm giá tối đa (VND) - chỉ áp dụng khi discount_type là PERCENTAGE

    @Column(nullable = false)
    private Integer active = 1; // 0 = ngừng hoạt động, 1 = hoạt động, 2 = tắt

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
