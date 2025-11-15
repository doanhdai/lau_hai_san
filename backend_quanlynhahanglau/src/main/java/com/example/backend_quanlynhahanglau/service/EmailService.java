package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.entity.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Gửi email thông báo bàn đã được chọn cho reservation
     */
    public void sendTableAssignedEmail(Reservation reservation) {
        try {
            // Kiểm tra customer có email không
            if (reservation.getCustomer() == null || 
                reservation.getCustomer().getEmail() == null || 
                reservation.getCustomer().getEmail().isBlank()) {
                log.warn("Không thể gửi email: Khách hàng không có email. Reservation ID: {}", 
                        reservation.getId());
                return;
            }

            // Kiểm tra reservation có bàn chưa
            if (reservation.getTable() == null) {
                log.warn("Không thể gửi email: Reservation chưa có bàn. Reservation ID: {}", 
                        reservation.getId());
                return;
            }

            String customerEmail = reservation.getCustomer().getEmail();
            String customerName = reservation.getCustomer().getFullName();
            String tableNumber = reservation.getTable().getTableNumber();
            String reservationTime = reservation.getReservationTime().format(DATE_TIME_FORMATTER);
            String numberOfGuests = String.valueOf(reservation.getNumberOfGuests());

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(customerEmail);
            message.setSubject("Xác nhận đặt bàn - Bàn đã được sắp xếp");
            
            String emailBody = buildTableAssignedEmailBody(
                    customerName, 
                    tableNumber, 
                    reservationTime, 
                    numberOfGuests,
                    reservation.getSpecialRequests()
            );
            
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("Đã gửi email thông báo bàn đã được chọn cho reservation ID: {} đến email: {}", 
                    reservation.getId(), customerEmail);
            
        } catch (Exception e) {
            log.error("Lỗi khi gửi email cho reservation ID: {}", reservation.getId(), e);
            // Không throw exception để không ảnh hưởng đến flow chính
        }
    }

    /**
     * Xây dựng nội dung email thông báo bàn đã được chọn
     */
    private String buildTableAssignedEmailBody(
            String customerName,
            String tableNumber,
            String reservationTime,
            String numberOfGuests,
            String specialRequests) {
        
        StringBuilder body = new StringBuilder();
        body.append("Kính chào ").append(customerName).append(",\n\n");
        body.append("Chúng tôi xin thông báo rằng bàn của quý khách đã được sắp xếp thành công!\n\n");
        body.append("Thông tin đặt bàn:\n");
        body.append("- Số bàn: ").append(tableNumber).append("\n");
        body.append("- Thời gian: ").append(reservationTime).append("\n");
        body.append("- Số lượng khách: ").append(numberOfGuests).append(" người\n");
        
        if (specialRequests != null && !specialRequests.isBlank()) {
            body.append("- Yêu cầu đặc biệt: ").append(specialRequests).append("\n");
        }
        
        body.append("\n");
        body.append("Chúng tôi rất mong được phục vụ quý khách!\n\n");
        body.append("Trân trọng,\n");
        body.append("Nhà hàng Lẩu Hải Sản");
        
        return body.toString();
    }
}

