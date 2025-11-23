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
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = com.example.backend_quanlynhahanglau.util.DateUtils.parseDateTime(startDate);
            LocalDateTime end = com.example.backend_quanlynhahanglau.util.DateUtils.parseDateTime(endDate);
            // If only date provided, set end time to end of day
            if (endDate.length() == 10) {
                end = end.withHour(23).withMinute(59).withSecond(59);
            }
            List<TableHistoryResponse> history = historyService.getHistoryByDateRange(start, end);
            return ResponseEntity.ok(ApiResponse.success(history));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid date format. Use yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss"));
        }
    }

    @GetMapping("/table/{tableId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<TableHistoryResponse>>> getHistoryByTableId(@PathVariable Long tableId) {
        List<TableHistoryResponse> history = historyService.getHistoryByTableId(tableId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
