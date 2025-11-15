package com.example.backend_quanlynhahanglau.dto.promotion;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class PromotionResponse {
    private Long id;
    private String name;
    private String promotionName; // Alias for name to match frontend
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscount;
    private Boolean active;
    private Integer status; // 0 = ngừng hoạt động, 1 = hoạt động, 2 = tắt
    private LocalDateTime createdAt;
    
    // Computed fields for frontend
    private String discountType; // "PERCENTAGE" or "FIXED"
    private BigDecimal discountValue; // Computed from discountPercent or discountAmount
    
    // Custom constructor for Builder
    public PromotionResponse(Long id, String name, String promotionName, String description,
                            LocalDateTime startDate, LocalDateTime endDate,
                            BigDecimal discountPercent, BigDecimal discountAmount,
                            BigDecimal minOrderValue, BigDecimal maxDiscount,
                            Boolean active, Integer status, LocalDateTime createdAt,
                            String discountType, BigDecimal discountValue) {
        this.id = id;
        this.name = name;
        this.promotionName = promotionName;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.discountPercent = discountPercent;
        this.discountAmount = discountAmount;
        this.minOrderValue = minOrderValue;
        this.maxDiscount = maxDiscount;
        this.active = active;
        this.status = status;
        this.createdAt = createdAt;
        this.discountType = discountType;
        this.discountValue = discountValue;
    }
}
