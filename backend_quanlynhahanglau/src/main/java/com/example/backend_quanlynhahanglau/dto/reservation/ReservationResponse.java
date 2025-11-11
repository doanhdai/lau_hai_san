package com.example.backend_quanlynhahanglau.dto.reservation;

import com.example.backend_quanlynhahanglau.enums.ReservationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {
    private Long id;
    private String customerName;
    private String customerPhone;
    private Long customerId;
    private String tableNumber;
    private Long tableId;
    private LocalDateTime reservationTime;
    private Integer numberOfGuests;
    private ReservationStatus status;
    private String specialRequests;
    private String notes;
    private Boolean emailSent;
    private String confirmedByName;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
}
