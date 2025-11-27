package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.entity.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        body.append("═══════════════════════════════════════\n");
        body.append("THÔNG TIN ĐẶT BÀN\n");
        body.append("═══════════════════════════════════════\n\n");
        body.append("- SỐ BÀN: ").append(tableNumber).append("\n");
        body.append("- Thời gian: ").append(reservationTime).append("\n");
        body.append("- Số lượng khách: ").append(numberOfGuests).append(" người\n");
        
        if (specialRequests != null && !specialRequests.isBlank()) {
            body.append("📝 Yêu cầu đặc biệt: ").append(specialRequests).append("\n");
        }
        
        body.append("\n");
        body.append("═══════════════════════════════════════\n");
        body.append("Vui lòng đến đúng giờ và đến đúng số bàn ").append(tableNumber).append(".\n");
        body.append("Chúng tôi rất mong được phục vụ quý khách!\n\n");
        body.append("Trân trọng,\n");
        body.append("Nhà hàng Lẩu Hải Sản");
        
        return body.toString();
    }

    /**
     * Gửi email thông báo thanh toán cọc thành công
     */
    public void sendDepositPaymentEmail(Reservation reservation, BigDecimal depositAmount, BigDecimal totalOrderAmount) {
        try {
            // Kiểm tra customer có email không
            if (reservation.getCustomer() == null || 
                reservation.getCustomer().getEmail() == null || 
                reservation.getCustomer().getEmail().isBlank()) {
                log.warn("Không thể gửi email: Khách hàng không có email. Reservation ID: {}", 
                        reservation.getId());
                return;
            }

            String customerEmail = reservation.getCustomer().getEmail();
            String customerName = reservation.getCustomer().getFullName();
            String reservationTime = reservation.getReservationTime().format(DATE_TIME_FORMATTER);
            String numberOfGuests = String.valueOf(reservation.getNumberOfGuests());

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(customerEmail);
            message.setSubject("Xác nhận thanh toán cọc đặt bàn thành công");
            
            String emailBody = buildDepositPaymentEmailBody(
                    customerName, 
                    reservation.getId(),
                    reservationTime, 
                    numberOfGuests,
                    depositAmount,
                    totalOrderAmount
            );
            
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("Đã gửi email thông báo thanh toán cọc cho reservation ID: {} đến email: {}", 
                    reservation.getId(), customerEmail);
            
        } catch (Exception e) {
            log.error("Lỗi khi gửi email thanh toán cọc cho reservation ID: {}", reservation.getId(), e);
            // Không throw exception để không ảnh hưởng đến flow chính
        }
    }

    /**
     * Xây dựng nội dung email thông báo thanh toán cọc thành công
     */
    private String buildDepositPaymentEmailBody(
            String customerName,
            Long reservationId,
            String reservationTime,
            String numberOfGuests,
            BigDecimal depositAmount,
            BigDecimal totalOrderAmount) {
        
        StringBuilder body = new StringBuilder();
        body.append("Kính chào ").append(customerName).append(",\n\n");
        body.append("Chúng tôi xin xác nhận đã nhận được thanh toán cọc của quý khách!\n\n");
        body.append("Thông tin đặt bàn:\n");
        body.append("- Mã đặt bàn: ").append(reservationId).append("\n");
        body.append("- Thời gian: ").append(reservationTime).append("\n");
        body.append("- Số lượng khách: ").append(numberOfGuests).append(" người\n");
        body.append("- Tổng đơn món: ").append(formatCurrency(totalOrderAmount)).append(" VNĐ\n");
        body.append("- Số tiền đã cọc (20%): ").append(formatCurrency(depositAmount)).append(" VNĐ\n");
        body.append("- Số tiền còn lại: ").append(formatCurrency(totalOrderAmount.subtract(depositAmount))).append(" VNĐ\n");
        
        body.append("\n");
        body.append("Quý khách vui lòng thanh toán số tiền còn lại khi đến nhà hàng.\n\n");
        body.append("Chúng tôi rất mong được phục vụ quý khách!\n\n");
        body.append("Trân trọng,\n");
        body.append("Nhà hàng Lẩu Hải Sản");
        
        return body.toString();
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0";
        return String.format("%,.0f", amount.doubleValue());
    }
}

