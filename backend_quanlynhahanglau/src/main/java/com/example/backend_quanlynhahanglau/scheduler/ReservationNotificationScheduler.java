package com.example.backend_quanlynhahanglau.scheduler;

import com.example.backend_quanlynhahanglau.dto.reservation.ReservationResponse;
import com.example.backend_quanlynhahanglau.repository.ReservationRepository;
import com.example.backend_quanlynhahanglau.service.ReservationService;
import com.example.backend_quanlynhahanglau.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationNotificationScheduler {

    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final WebSocketNotificationService webSocketNotificationService;
    
    // Track reservations that have already been notified (to avoid duplicate notifications)
    private final Set<Long> notifiedReservationIds = new HashSet<>();
    
    // Clean up old notification IDs every hour
    private LocalDateTime lastCleanup = LocalDateTime.now();

    /**
     * Check for reservations coming soon (30 minutes before arrival) and send notifications
     * Runs every 1 minute
     */
    @Scheduled(fixedRate = 60000) // Every 60 seconds (1 minute)
    public void checkComingSoonReservations() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime thirtyMinutesFromNow = now.plusMinutes(30);
            
            // Find reservations that are coming soon (within next 30 minutes)
            var upcomingReservations = reservationRepository.findUpcomingReservations(now, thirtyMinutesFromNow);
            
            for (var reservation : upcomingReservations) {
                // Skip if already notified
                if (notifiedReservationIds.contains(reservation.getId())) {
                    continue;
                }
                
                // Calculate minutes until reservation
                long minutesUntil = java.time.Duration.between(now, reservation.getReservationTime()).toMinutes();
                
                // Only notify if within 30 minutes window (avoid notifying too early or too late)
                if (minutesUntil >= 0 && minutesUntil <= 30) {
                    try {
                        ReservationResponse response = reservationService.getReservationById(reservation.getId());
                        webSocketNotificationService.notifyComingSoonReservation(response);
                        
                        // Mark as notified
                        notifiedReservationIds.add(reservation.getId());
                        
                        log.info("Sent coming soon notification for reservation ID: {}, minutes until: {}", 
                                reservation.getId(), minutesUntil);
                    } catch (Exception e) {
                        log.error("Error sending coming soon notification for reservation ID: {}", 
                                reservation.getId(), e);
                    }
                }
            }
            
            // Clean up old notification IDs (reservations that have passed)
            if (lastCleanup.plusHours(1).isBefore(now)) {
                cleanupOldNotificationIds(now);
                lastCleanup = now;
            }
            
        } catch (Exception e) {
            log.error("Error in checkComingSoonReservations scheduler", e);
        }
    }
    
    /**
     * Remove notification IDs for reservations that have already passed
     */
    private void cleanupOldNotificationIds(LocalDateTime now) {
        notifiedReservationIds.removeIf(reservationId -> {
            try {
                var reservation = reservationRepository.findById(reservationId);
                if (reservation.isEmpty()) {
                    return true; // Reservation no longer exists
                }
                // Remove if reservation time has passed more than 1 hour ago
                return reservation.get().getReservationTime().plusHours(1).isBefore(now);
            } catch (Exception e) {
                log.warn("Error checking reservation {} for cleanup", reservationId, e);
                return true; // Remove on error
            }
        });
        log.debug("Cleaned up notification IDs. Remaining: {}", notifiedReservationIds.size());
    }
}

