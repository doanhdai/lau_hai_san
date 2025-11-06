package com.example.backend_quanlynhahanglau.controller;

import com.example.backend_quanlynhahanglau.dto.ApiResponse;
import com.example.backend_quanlynhahanglau.dto.report.RevenueReportResponse;
import com.example.backend_quanlynhahanglau.dto.report.SalesReportResponse;
import com.example.backend_quanlynhahanglau.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReportController {
    private final ReportService reportService;
    
    @GetMapping("/revenue")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<RevenueReportResponse>> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        RevenueReportResponse report = reportService.getRevenueReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }
    
    @GetMapping("/sales")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<SalesReportResponse>> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        SalesReportResponse report = reportService.getSalesReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
