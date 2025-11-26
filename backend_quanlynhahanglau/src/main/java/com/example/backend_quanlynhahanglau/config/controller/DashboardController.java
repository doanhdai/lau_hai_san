package com.example.backend_quanlynhahanglau.config.controller;

import com.example.backend_quanlynhahanglau.dto.ApiResponse;
import com.example.backend_quanlynhahanglau.dto.report.BestSellingDish;
import com.example.backend_quanlynhahanglau.dto.report.DashboardStats;
import com.example.backend_quanlynhahanglau.dto.report.RevenueReport;
import com.example.backend_quanlynhahanglau.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF')")
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboardStats() {
        DashboardStats stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<RevenueReport>> getRevenueReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = com.example.backend_quanlynhahanglau.util.DateUtils.parseDateTime(startDate);
            LocalDateTime end = com.example.backend_quanlynhahanglau.util.DateUtils.parseDateTime(endDate);
            if (endDate.length() == 10) {
                end = end.withHour(23).withMinute(59).withSecond(59);
            }
            RevenueReport report = dashboardService.getRevenueReport(start, end);
            return ResponseEntity.ok(ApiResponse.success(report));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid date format. Use yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss"));
        }
    }

    @GetMapping("/best-selling-dishes")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF')")
    public ResponseEntity<ApiResponse<List<BestSellingDish>>> getBestSellingDishes(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        List<BestSellingDish> dishes = dashboardService.getBestSellingDishes(limit);
        return ResponseEntity.ok(ApiResponse.success(dishes));
    }

    @GetMapping("/monthly-revenue")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<Map<Integer, BigDecimal>>> getMonthlyRevenue(
            @RequestParam Integer year) {
        Map<Integer, BigDecimal> revenue = dashboardService.getMonthlyRevenue(year);
        return ResponseEntity.ok(ApiResponse.success(revenue));
    }

    @GetMapping("/export/revenue-excel")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<byte[]> exportRevenueToExcel(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = com.example.backend_quanlynhahanglau.util.DateUtils.parseDateTime(startDate);
            LocalDateTime end = com.example.backend_quanlynhahanglau.util.DateUtils.parseDateTime(endDate);
            if (endDate.length() == 10) {
                end = end.withHour(23).withMinute(59).withSecond(59);
            }
            RevenueReport report = dashboardService.getRevenueReport(start, end);
            byte[] excelData = dashboardService.exportRevenueToExcel(report);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "bao-cao-doanh-thu.xlsx");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/export/revenue-pdf")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<byte[]> exportRevenueToPdf(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = com.example.backend_quanlynhahanglau.util.DateUtils.parseDateTime(startDate);
            LocalDateTime end = com.example.backend_quanlynhahanglau.util.DateUtils.parseDateTime(endDate);
            if (endDate.length() == 10) {
                end = end.withHour(23).withMinute(59).withSecond(59);
            }
            RevenueReport report = dashboardService.getRevenueReport(start, end);
            byte[] pdfData = dashboardService.exportRevenueToPdf(report);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "bao-cao-doanh-thu.pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/export/best-selling-excel")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<byte[]> exportBestSellingDishesToExcel(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        try {
            List<BestSellingDish> dishes = dashboardService.getBestSellingDishes(limit);
            byte[] excelData = dashboardService.exportBestSellingDishesToExcel(dishes);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "top-mon-ban-chay.xlsx");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/export/best-selling-pdf")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<byte[]> exportBestSellingDishesToPdf(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        try {
            List<BestSellingDish> dishes = dashboardService.getBestSellingDishes(limit);
            byte[] pdfData = dashboardService.exportBestSellingDishesToPdf(dishes);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "top-mon-ban-chay.pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
