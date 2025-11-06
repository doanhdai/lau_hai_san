package com.example.backend_quanlynhahanglau.dto.promotion;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private Boolean active;
    private LocalDateTime createdAt;
}
