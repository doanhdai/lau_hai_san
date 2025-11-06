package com.example.backend_quanlynhahanglau.dto.report;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueReport {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private BigDecimal averageOrderValue;
    private BigDecimal totalDiscount;
    private BigDecimal totalTax;
}
