package com.example.backend_quanlynhahanglau.config.controller;

import com.example.backend_quanlynhahanglau.dto.ApiResponse;
import com.example.backend_quanlynhahanglau.dto.table.TableRequest;
import com.example.backend_quanlynhahanglau.dto.table.TableResponse;
import com.example.backend_quanlynhahanglau.enums.TableStatus;
import com.example.backend_quanlynhahanglau.service.TableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class TableController {
    private final TableService tableService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TableResponse>>> getAllTables() {
        List<TableResponse> tables = tableService.getAllTables();
        return ResponseEntity.ok(ApiResponse.success(tables));
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TableResponse>>> getAvailableTables() {
        List<TableResponse> tables = tableService.getAvailableTables();
        return ResponseEntity.ok(ApiResponse.success(tables));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<TableResponse>>> getAvailableTablesByTime(
            @RequestParam String reservationTime,
            @RequestParam(required = false) Integer numberOfGuests) {
        try {
            LocalDateTime dateTime = com.example.backend_quanlynhahanglau.util.DateUtils.parseDateTime(reservationTime);
            List<TableResponse> tables = tableService.getAvailableTablesByTime(dateTime, numberOfGuests);
            return ResponseEntity.ok(ApiResponse.success(tables));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid date format. Use yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss"));
        }
    }

    @GetMapping("/check-availability")
    public ResponseEntity<ApiResponse<Boolean>> checkTableAvailability(
            @RequestParam String reservationTime,
            @RequestParam(required = false) Integer numberOfGuests) {
        try {
            if (numberOfGuests == null || numberOfGuests <= 0) {
                return ResponseEntity.ok(ApiResponse.success(false));
            }
            LocalDateTime dateTime = com.example.backend_quanlynhahanglau.util.DateUtils.parseDateTime(reservationTime);
            Boolean isAvailable = tableService.checkTableAvailability(dateTime, numberOfGuests);
            return ResponseEntity.ok(ApiResponse.success(isAvailable));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid date format. Use yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss"));
        }
    }

    @GetMapping("/suitable")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<TableResponse>>> getSuitableTables(@RequestParam Integer numberOfGuests) {
        List<TableResponse> tables = tableService.getSuitableTables(numberOfGuests);
        return ResponseEntity.ok(ApiResponse.success(tables));
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<TableResponse>> getTableById(
            @PathVariable("id") Long id) {
        TableResponse table = tableService.getTableById(id);
        return ResponseEntity.ok(ApiResponse.success(table));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<TableResponse>> createTable(@Valid @RequestBody TableRequest request) {
        TableResponse table = tableService.createTable(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo bàn thành công", table));
    }

    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<TableResponse>> updateTable(
            @PathVariable Long id,
            @Valid @RequestBody TableRequest request) {
        TableResponse table = tableService.updateTable(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật bàn thành công", table));
    }

    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTable(@PathVariable Long id) {
        tableService.deleteTable(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa bàn thành công", null));
    }

    @PutMapping("/{id:\\d+}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<Void>> updateTableStatus(
            @PathVariable Long id,
            @RequestParam TableStatus status) {
        tableService.updateTableStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái bàn thành công", null));
    }

    @PutMapping("/{id:\\d+}/position")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<TableResponse>> updateTablePosition(
            @PathVariable Long id,
            @RequestParam Integer positionX,
            @RequestParam Integer positionY) {
        TableResponse table = tableService.updateTablePosition(id, positionX, positionY);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật vị trí bàn thành công", table));
    }
}
