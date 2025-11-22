package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.dto.report.RevenueReportResponse;
import com.example.backend_quanlynhahanglau.dto.report.ReportStatsResponse;
import com.example.backend_quanlynhahanglau.dto.report.SalesReportResponse;
import com.example.backend_quanlynhahanglau.entity.Order;
import com.example.backend_quanlynhahanglau.entity.OrderDetail;
import com.example.backend_quanlynhahanglau.enums.OrderStatus;
import com.example.backend_quanlynhahanglau.enums.PaymentStatus;
import com.example.backend_quanlynhahanglau.repository.OrderDetailRepository;
import com.example.backend_quanlynhahanglau.repository.OrderRepository;
import com.example.backend_quanlynhahanglau.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final PaymentRepository paymentRepository;
    
    @Transactional(readOnly = true)
    public RevenueReportResponse getRevenueReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        List<Order> orders = orderRepository.findByDate(startDateTime, endDateTime)
                .stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .collect(Collectors.toList());
        
        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long totalOrders = orders.size();
        
        BigDecimal averageOrderValue = totalOrders > 0 
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        // Group by date
        Map<LocalDate, List<Order>> ordersByDate = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().toLocalDate()));
        
        List<RevenueReportResponse.DailyRevenueData> dailyRevenue = new ArrayList<>();
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            List<Order> dayOrders = ordersByDate.getOrDefault(currentDate, Collections.emptyList());
            
            BigDecimal dayRevenue = dayOrders.stream()
                    .map(Order::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            dailyRevenue.add(RevenueReportResponse.DailyRevenueData.builder()
                    .date(currentDate)
                    .revenue(dayRevenue)
                    .orderCount((long) dayOrders.size())
                    .build());
            
            currentDate = currentDate.plusDays(1);
        }
        
        return RevenueReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .averageOrderValue(averageOrderValue)
                .dailyRevenue(dailyRevenue)
                .build();
    }
    
    @Transactional(readOnly = true)
    public SalesReportResponse getSalesReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        List<Order> completedOrders = orderRepository.findByDate(startDateTime, endDateTime)
                .stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .collect(Collectors.toList());
        
        // Get all order details from completed orders
        List<OrderDetail> allDetails = completedOrders.stream()
                .flatMap(o -> o.getOrderDetails().stream())
                .collect(Collectors.toList());
        
        // Group by dish
        Map<Long, List<OrderDetail>> detailsByDish = allDetails.stream()
                .collect(Collectors.groupingBy(d -> d.getDish().getId()));
        
        List<SalesReportResponse.DishSalesData> topSellingDishes = detailsByDish.entrySet().stream()
                .map(entry -> {
                    List<OrderDetail> details = entry.getValue();
                    OrderDetail sample = details.get(0);
                    
                    Long quantitySold = details.stream()
                            .mapToLong(OrderDetail::getQuantity)
                            .sum();
                    
                    BigDecimal revenue = details.stream()
                            .map(OrderDetail::getSubtotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    return SalesReportResponse.DishSalesData.builder()
                            .dishId(sample.getDish().getId())
                            .dishName(sample.getDish().getName())
                            .categoryName(sample.getDish().getCategory().getName())
                            .quantitySold(quantitySold)
                            .revenue(revenue)
                            .build();
                })
                .sorted((a, b) -> b.getQuantitySold().compareTo(a.getQuantitySold()))
                .limit(10)
                .collect(Collectors.toList());
        
        // Group by category
        Map<String, List<OrderDetail>> detailsByCategory = allDetails.stream()
                .collect(Collectors.groupingBy(d -> d.getDish().getCategory().getName()));
        
        List<SalesReportResponse.CategorySalesData> salesByCategory = detailsByCategory.entrySet().stream()
                .map(entry -> {
                    List<OrderDetail> details = entry.getValue();
                    
                    Long itemsSold = details.stream()
                            .mapToLong(OrderDetail::getQuantity)
                            .sum();
                    
                    BigDecimal revenue = details.stream()
                            .map(OrderDetail::getSubtotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    return SalesReportResponse.CategorySalesData.builder()
                            .categoryName(entry.getKey())
                            .itemsSold(itemsSold)
                            .revenue(revenue)
                            .build();
                })
                .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue()))
                .collect(Collectors.toList());
        
        BigDecimal totalRevenue = allDetails.stream()
                .map(OrderDetail::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Long totalItemsSold = allDetails.stream()
                .mapToLong(OrderDetail::getQuantity)
                .sum();
        
        return SalesReportResponse.builder()
                .topSellingDishes(topSellingDishes)
                .salesByCategory(salesByCategory)
                .totalRevenue(totalRevenue)
                .totalItemsSold(totalItemsSold)
                .build();
    }
    
    @Transactional(readOnly = true)
    public ReportStatsResponse getReportStats(String filterType, LocalDate startDate, LocalDate endDate) {
        // Xác định khoảng thời gian dựa trên filterType
        LocalDate actualStartDate;
        LocalDate actualEndDate;
        
        LocalDate today = LocalDate.now();
        
        switch (filterType.toUpperCase()) {
            case "TODAY":
                actualStartDate = today;
                actualEndDate = today;
                break;
            case "THIS_MONTH":
                actualStartDate = today.withDayOfMonth(1);
                actualEndDate = today;
                break;
            case "THIS_YEAR":
                actualStartDate = today.withDayOfYear(1);
                actualEndDate = today;
                break;
            case "CUSTOM":
                actualStartDate = startDate != null ? startDate : today;
                actualEndDate = endDate != null ? endDate : today;
                break;
            default:
                actualStartDate = today;
                actualEndDate = today;
        }
        
        LocalDateTime startDateTime = actualStartDate.atStartOfDay();
        LocalDateTime endDateTime = actualEndDate.atTime(LocalTime.MAX);
        
        // Lấy tất cả đơn hàng trong khoảng thời gian (không lọc theo status)
        List<Order> allOrders = orderRepository.findByDate(startDateTime, endDateTime);
        
        // Tổng doanh thu = SUM(amount) từ payments COMPLETED
        BigDecimal totalRevenue = paymentRepository.calculateTotalAmountByDateAndStatus(
                startDateTime, endDateTime, PaymentStatus.COMPLETED);
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }
        
        // Tổng đã giảm = SUM(discount) từ orders
        BigDecimal totalDiscount = allOrders.stream()
                .map(o -> o.getDiscount() != null ? o.getDiscount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Số đơn hàng (tất cả đơn trong khoảng thời gian)
        Long totalOrders = (long) allOrders.size();
        
        // Doanh thu trung bình
        BigDecimal averageRevenue = totalOrders > 0 && totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        // Nhóm theo ngày để tạo dữ liệu cho biểu đồ
        Map<LocalDate, List<Order>> ordersByDate = allOrders.stream()
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().toLocalDate()));
        
        // Lấy doanh thu từ payments theo ngày (chỉ lấy payments có status COMPLETED)
        // QUAN TRỌNG: Doanh thu phải lấy từ bảng payments, không phải từ orders
        // Truyền string name của enum để tương thích với SQL Server
        List<Object[]> paymentsByDate = paymentRepository.calculateAmountByDateAndStatus(
                startDateTime, endDateTime, PaymentStatus.COMPLETED.name());
        Map<LocalDate, BigDecimal> revenueByDateMap = new HashMap<>();
        for (Object[] row : paymentsByDate) {
            LocalDate date;
            // Native query trả về java.sql.Date
            if (row[0] instanceof java.sql.Date) {
                date = ((java.sql.Date) row[0]).toLocalDate();
            } else if (row[0] instanceof java.util.Date) {
                date = ((java.util.Date) row[0]).toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
            } else if (row[0] instanceof LocalDate) {
                date = (LocalDate) row[0];
            } else if (row[0] instanceof String) {
                // Nếu là string, parse thành LocalDate
                date = LocalDate.parse((String) row[0]);
            } else {
                continue;
            }
            BigDecimal amount = (BigDecimal) row[1];
            if (amount != null) {
                revenueByDateMap.put(date, amount);
            }
        }
        
        // Tạo dữ liệu cho biểu đồ
        List<ReportStatsResponse.DailyData> revenueByDay = new ArrayList<>();
        List<ReportStatsResponse.DailyData> ordersByDay = new ArrayList<>();
        List<ReportStatsResponse.DailyData> discountByDay = new ArrayList<>();
        
        LocalDate currentDate = actualStartDate;
        while (!currentDate.isAfter(actualEndDate)) {
            List<Order> dayOrders = ordersByDate.getOrDefault(currentDate, Collections.emptyList());
            
            // Doanh thu theo ngày (từ payments)
            BigDecimal dayRevenue = revenueByDateMap.getOrDefault(currentDate, BigDecimal.ZERO);
            revenueByDay.add(ReportStatsResponse.DailyData.builder()
                    .date(currentDate)
                    .value(dayRevenue)
                    .build());
            
            // Số đơn hàng theo ngày
            ordersByDay.add(ReportStatsResponse.DailyData.builder()
                    .date(currentDate)
                    .value(BigDecimal.valueOf(dayOrders.size()))
                    .build());
            
            // Đã giảm giá theo ngày (từ orders.discount)
            BigDecimal dayDiscount = dayOrders.stream()
                    .map(o -> o.getDiscount() != null ? o.getDiscount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            discountByDay.add(ReportStatsResponse.DailyData.builder()
                    .date(currentDate)
                    .value(dayDiscount)
                    .build());
            
            currentDate = currentDate.plusDays(1);
        }
        
        return ReportStatsResponse.builder()
                .filterType(filterType)
                .startDate(actualStartDate)
                .endDate(actualEndDate)
                .totalRevenue(totalRevenue)
                .totalDiscount(totalDiscount)
                .totalOrders(totalOrders)
                .averageRevenue(averageRevenue)
                .revenueByDay(revenueByDay)
                .ordersByDay(ordersByDay)
                .discountByDay(discountByDay)
                .build();
    }
}
