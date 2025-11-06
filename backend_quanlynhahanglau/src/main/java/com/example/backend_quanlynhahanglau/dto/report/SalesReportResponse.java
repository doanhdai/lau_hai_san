package com.example.backend_quanlynhahanglau.dto.report;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesReportResponse {
    private List<DishSalesData> topSellingDishes;
    private List<CategorySalesData> salesByCategory;
    private BigDecimal totalRevenue;
    private Long totalItemsSold;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DishSalesData {
        private Long dishId;
        private String dishName;
        private String categoryName;
        private Long quantitySold;
        private BigDecimal revenue;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategorySalesData {
        private String categoryName;
        private Long itemsSold;
        private BigDecimal revenue;
    }
}
