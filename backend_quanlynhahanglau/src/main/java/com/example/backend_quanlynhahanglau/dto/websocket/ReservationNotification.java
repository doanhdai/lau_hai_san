package com.example.backend_quanlynhahanglau.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationNotification {
    private Long reservationId;
    private String customerName;
    private String tableNumber;
    private LocalDateTime reservationTime;
    private Integer numberOfGuests;
    private String status;
    private String type; // "NEW" or "COMING_SOON"
    private String message;
}

