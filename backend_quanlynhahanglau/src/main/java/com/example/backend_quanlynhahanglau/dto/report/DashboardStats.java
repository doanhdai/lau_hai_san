package com.example.backend_quanlynhahanglau.dto.report;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStats {
    private Long totalCustomers;
    private Long vipCustomers;
    private Long totalTables;
    private Long availableTables;
    private Long occupiedTables;
    private Long totalDishes;
    private Long availableDishes;
    private Long totalOrders;
    private Long pendingOrders;
    private Long completedOrders;
    private BigDecimal todayRevenue;
    private BigDecimal monthRevenue;
    private BigDecimal yearRevenue;
    private Map<String, Long> tableStatusCount;
    private Map<String, Long> orderStatusCount;
    private Double averageRating;
}
