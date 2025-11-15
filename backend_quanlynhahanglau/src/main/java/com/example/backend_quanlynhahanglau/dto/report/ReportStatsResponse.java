package com.example.backend_quanlynhahanglau.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportStatsResponse {
    private String filterType; // "TODAY", "THIS_MONTH", "THIS_YEAR", "CUSTOM"
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Thống kê tổng quan
    private BigDecimal totalRevenue;        // Tổng doanh thu
    private BigDecimal totalReceived;       // Tổng thực nhận (từ payments COMPLETED)
    private Long totalOrders;               // Số đơn hàng
    private BigDecimal averageRevenue;      // Doanh thu trung bình
    
    // Dữ liệu cho biểu đồ
    private List<DailyData> revenueByDay;   // Doanh thu theo ngày
    private List<DailyData> ordersByDay;     // Đơn hàng theo ngày
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyData {
        private LocalDate date;
        private BigDecimal value;  // Doanh thu hoặc số đơn hàng
    }
}

