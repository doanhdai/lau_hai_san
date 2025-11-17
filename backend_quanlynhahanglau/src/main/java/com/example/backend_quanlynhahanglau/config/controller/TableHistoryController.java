package com.example.backend_quanlynhahanglau.config.controller;

import com.example.backend_quanlynhahanglau.dto.ApiResponse;
import com.example.backend_quanlynhahanglau.dto.history.TableHistoryResponse;
import com.example.backend_quanlynhahanglau.service.TableHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/table-history")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class TableHistoryController {
    private final TableHistoryService historyService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<TableHistoryResponse>>> getAllHistory() {
        List<TableHistoryResponse> history = historyService.getAllHistory();
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/by-date")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<TableHistoryResponse>>> getHistoryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<TableHistoryResponse> history = historyService.getHistoryByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/table/{tableId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<TableHistoryResponse>>> getHistoryByTableId(@PathVariable Long tableId) {
        List<TableHistoryResponse> history = historyService.getHistoryByTableId(tableId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
