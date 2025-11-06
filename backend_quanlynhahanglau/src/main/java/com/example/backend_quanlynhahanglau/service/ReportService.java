package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.dto.report.RevenueReportResponse;
import com.example.backend_quanlynhahanglau.dto.report.SalesReportResponse;
import com.example.backend_quanlynhahanglau.entity.Order;
import com.example.backend_quanlynhahanglau.entity.OrderDetail;
import com.example.backend_quanlynhahanglau.enums.OrderStatus;
import com.example.backend_quanlynhahanglau.repository.OrderDetailRepository;
import com.example.backend_quanlynhahanglau.repository.OrderRepository;
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
}
