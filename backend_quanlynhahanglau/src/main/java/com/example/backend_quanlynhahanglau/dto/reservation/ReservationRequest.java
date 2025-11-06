package com.example.backend_quanlynhahanglau.dto.reservation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationRequest {
    // For existing customer
    private Long customerId;

    // For new customer (if customerId is null)
    private String customerName;
    
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String customerPhone;
    
    private String customerEmail;

    // Phải có tableId hoặc roomId (ít nhất một trong hai)
    private Long tableId;
    private Long roomId;

    @NotNull(message = "Thời gian đặt bàn không được để trống")
    private LocalDateTime reservationDateTime;

    @NotNull(message = "Số lượng khách không được để trống")
    @Positive(message = "Số lượng khách phải là số dương")
    private Integer numberOfGuests;

    private String specialRequests;
    private String notes;
    private String status;
}
