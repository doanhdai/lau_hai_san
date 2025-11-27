package com.example.backend_quanlynhahanglau.config.controller;

import com.example.backend_quanlynhahanglau.dto.ApiResponse;
import com.example.backend_quanlynhahanglau.dto.reservation.PublicReservationRequest;
import com.example.backend_quanlynhahanglau.dto.reservation.ReservationRequest;
import com.example.backend_quanlynhahanglau.dto.reservation.ReservationResponse;
import com.example.backend_quanlynhahanglau.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReservationController {
    private final ReservationService reservationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getAllReservations() {
        List<ReservationResponse> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(ApiResponse.success(reservations));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservationById(@PathVariable Long id) {
        ReservationResponse reservation = reservationService.getReservationById(id);
        return ResponseEntity.ok(ApiResponse.success(reservation));
    }

    @GetMapping("/by-date")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getReservationsByDate(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = parseDateTime(startDate);
            LocalDateTime end = parseDateTime(endDate);
            if (endDate.length() == 10) {
                end = end.withHour(23).withMinute(59).withSecond(59);
            }
            List<ReservationResponse> reservations = reservationService.getReservationsByDate(start, end);
            return ResponseEntity.ok(ApiResponse.success(reservations));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid date format. Use yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss"));
        }
    }
    
    private LocalDateTime parseDateTime(String dateTimeStr) {
        return com.example.backend_quanlynhahanglau.util.DateUtils.parseDateTime(dateTimeStr);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> searchByCustomerName(
            @RequestParam String customerName) {
        List<ReservationResponse> reservations = reservationService.searchByCustomerName(customerName);
        return ResponseEntity.ok(ApiResponse.success(reservations));
    }

    @GetMapping("/public/user/{userId}")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getReservationHistoryByUserId(
            @PathVariable Long userId) {
        List<ReservationResponse> reservations = reservationService.getReservationsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử đặt bàn thành công", reservations));
    }

    @PostMapping("/public")
    public ResponseEntity<ApiResponse<ReservationResponse>> createPublicReservation(
            @Valid @RequestBody PublicReservationRequest request) {
        ReservationResponse reservation = reservationService.createPublicReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đặt bàn thành công", reservation));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @Valid @RequestBody ReservationRequest request) {
        ReservationResponse reservation = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo đặt bàn thành công", reservation));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<ReservationResponse>> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationRequest request) {
        ReservationResponse reservation = reservationService.updateReservation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật đặt bàn thành công", reservation));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa đặt bàn thành công", null));
    }

    @PutMapping("/public/{id}/cancel")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelPublicReservation(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        ReservationResponse reservation = reservationService.cancelPublicReservation(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Hủy đặt bàn thành công", reservation));
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<ReservationResponse>> confirmReservation(@PathVariable Long id) {
        ReservationResponse reservation = reservationService.confirmReservation(id);
        return ResponseEntity.ok(ApiResponse.success("Xác nhận đặt bàn thành công", reservation));
    }

    @PutMapping("/{id}/checkin")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<ReservationResponse>> checkInReservation(@PathVariable Long id) {
        ReservationResponse reservation = reservationService.checkInReservation(id);
        return ResponseEntity.ok(ApiResponse.success("Check-in thành công", reservation));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(@PathVariable Long id) {
        ReservationResponse reservation = reservationService.cancelReservation(id);
        return ResponseEntity.ok(ApiResponse.success("Hủy đặt bàn thành công", reservation));
    }

    @PutMapping("/{id}/table")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<ReservationResponse>> updateReservationTable(
            @PathVariable Long id,
            @RequestParam(required = false) Long tableId) {
        ReservationResponse reservation = reservationService.updateReservationTable(id, tableId);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật bàn cho đặt bàn thành công", reservation));
    }

    @PutMapping("/{id}/transfer-table")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<ReservationResponse>> transferTable(
            @PathVariable Long id,
            @RequestParam Long newTableId) {
        ReservationResponse reservation = reservationService.transferTable(id, newTableId);
        return ResponseEntity.ok(ApiResponse.success("Đổi bàn thành công", reservation));
    }

    @PostMapping("/public/{id}/pay-deposit")
    public ResponseEntity<ApiResponse<ReservationResponse>> payDeposit(
            @PathVariable Long id,
            @RequestParam String depositAmount) {
        try {
            java.math.BigDecimal deposit = new java.math.BigDecimal(depositAmount);
            ReservationResponse reservation = reservationService.payDeposit(id, deposit);
            return ResponseEntity.ok(ApiResponse.success("Thanh toán cọc thành công", reservation));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Số tiền cọc không hợp lệ: " + depositAmount));
        }
    }

    @PostMapping("/public/with-deposit")
    public ResponseEntity<ApiResponse<ReservationResponse>> createPublicReservationWithDeposit(
            @Valid @RequestBody com.example.backend_quanlynhahanglau.dto.reservation.PublicReservationWithDepositRequest request) {
        ReservationResponse reservation = reservationService.createPublicReservationWithDeposit(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đặt bàn và thanh toán cọc thành công", reservation));
    }
}
