package com.example.backend_quanlynhahanglau.dto.report;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BestSellingDish {
    private Long dishId;
    private String dishName;
    private String categoryName;
    private Long totalQuantitySold;
    private BigDecimal price;
    private BigDecimal totalRevenue;
}
