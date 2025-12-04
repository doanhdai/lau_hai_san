package com.example.backend_quanlynhahanglau.entity;

import com.example.backend_quanlynhahanglau.enums.DishStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "dishes")
@SQLDelete(sql = "UPDATE dishes SET active = false WHERE id = ?")
// Bỏ @Where để các đơn cũ vẫn load được món đã xóa
// Filter thủ công trong service layer khi cần
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private DishCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DishStatus status = DishStatus.AVAILABLE;

    @Column(length = 500)
    private String imageUrl;

    @Column(columnDefinition = "bit default 0")
    private Boolean isPromotion = false; // Món thuộc chương trình khuyến mãi

    @ManyToOne
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @Column(columnDefinition = "bit default 1")
    private Boolean active = true; // Cột dùng cho soft delete

    @Column(name = "estimated_preparation_time")
    private Integer estimatedPreparationTime = 30; // Thời gian dự kiến ra món (phút), mặc định 30 phút

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
