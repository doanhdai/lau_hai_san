package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.dto.reservation.ReservationResponse;
import com.example.backend_quanlynhahanglau.dto.websocket.ReservationNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyNewReservation(ReservationResponse reservation) {
        try {
            String customerName = reservation.getCustomerName() != null && !reservation.getCustomerName().isBlank()
                    ? reservation.getCustomerName()
                    : "Khách hàng";
            
            String tableNumber = reservation.getTableNumber() != null 
                    ? reservation.getTableNumber() 
                    : "Chưa gán bàn";
            
            ReservationNotification notification = ReservationNotification.builder()
                    .reservationId(reservation.getId())
                    .customerName(customerName)
                    .tableNumber(tableNumber)
                    .reservationTime(reservation.getReservationTime())
                    .numberOfGuests(reservation.getNumberOfGuests())
                    .status(reservation.getStatus() != null ? reservation.getStatus().name() : "PENDING")
                    .type("NEW")
                    .message(String.format("Đặt bàn mới: %s - %s", customerName, tableNumber))
                    .build();

            messagingTemplate.convertAndSend("/topic/reservations", notification);
            log.info("Sent WebSocket notification for new reservation: {}", reservation.getId());
        } catch (Exception e) {
            log.error("Error sending WebSocket notification for reservation: {}", reservation.getId(), e);
        }
    }

    public void notifyComingSoonReservation(ReservationResponse reservation) {
        try {
            LocalDateTime reservationTime = reservation.getReservationTime();
            LocalDateTime now = LocalDateTime.now();
            long minutesUntil = java.time.Duration.between(now, reservationTime).toMinutes();

            String customerName = reservation.getCustomerName() != null && !reservation.getCustomerName().isBlank()
                    ? reservation.getCustomerName()
                    : "Khách hàng";
            
            String tableNumber = reservation.getTableNumber() != null 
                    ? reservation.getTableNumber() 
                    : "Chưa gán bàn";

            ReservationNotification notification = ReservationNotification.builder()
                    .reservationId(reservation.getId())
                    .customerName(customerName)
                    .tableNumber(tableNumber)
                    .reservationTime(reservation.getReservationTime())
                    .numberOfGuests(reservation.getNumberOfGuests())
                    .status(reservation.getStatus() != null ? reservation.getStatus().name() : "CONFIRMED")
                    .type("COMING_SOON")
                    .message(String.format("Khách sắp đến: %s - Còn %d phút - %s", customerName, minutesUntil, tableNumber))
                    .build();

            messagingTemplate.convertAndSend("/topic/reservations", notification);
            log.info("Sent WebSocket notification for coming soon reservation: {}", reservation.getId());
        } catch (Exception e) {
            log.error("Error sending WebSocket notification for coming soon reservation: {}", reservation.getId(), e);
        }
    }
}

