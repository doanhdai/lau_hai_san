package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.dto.payment.PaymentRequest;
import com.example.backend_quanlynhahanglau.entity.Order;
import com.example.backend_quanlynhahanglau.entity.Payment;
import com.example.backend_quanlynhahanglau.exception.BadRequestException;
import com.example.backend_quanlynhahanglau.exception.ResourceNotFoundException;
import com.example.backend_quanlynhahanglau.repository.OrderRepository;
import com.example.backend_quanlynhahanglau.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Payment createPayment(PaymentRequest request) {
        // Tìm order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", "id", request.getOrderId()));

        // Tính số tiền cần thanh toán (trừ số tiền đã cọc nếu có)
        BigDecimal amountToPay = request.getAmount();
        BigDecimal depositAmount = BigDecimal.ZERO;
        
        // Kiểm tra xem order có reservation không và reservation có cọc không
        if (order.getReservation() != null && order.getReservation().getDepositAmount() != null) {
            depositAmount = order.getReservation().getDepositAmount();
            
            // Tính số tiền còn lại sau khi trừ cọc
            BigDecimal remainingAmount = order.getTotal().subtract(depositAmount);
            
            // Kiểm tra số tiền thanh toán có đúng không (cho phép sai số nhỏ)
            BigDecimal difference = amountToPay.subtract(remainingAmount).abs();
            if (difference.compareTo(BigDecimal.valueOf(0.01)) > 0) {
                throw new BadRequestException(
                        String.format("Số tiền thanh toán không đúng. Tổng đơn: %s VNĐ, Đã cọc: %s VNĐ, Cần thanh toán: %s VNĐ",
                                formatCurrency(order.getTotal()),
                                formatCurrency(depositAmount),
                                formatCurrency(remainingAmount)));
            }
            
            log.info("Order {} has deposit {} VNĐ, remaining amount: {} VNĐ", 
                    order.getId(), depositAmount, remainingAmount);
        } else {
            // Không có cọc, kiểm tra số tiền thanh toán có đúng không
            BigDecimal difference = amountToPay.subtract(order.getTotal()).abs();
            if (difference.compareTo(BigDecimal.valueOf(0.01)) > 0) {
                throw new BadRequestException(
                        String.format("Số tiền thanh toán không đúng. Tổng đơn: %s VNĐ",
                                formatCurrency(order.getTotal())));
            }
        }

        // Tạo payment với số tiền thực tế thanh toán (sau khi trừ cọc)
        Payment payment = Payment.builder()
                .order(order)
                .amount(amountToPay)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(request.getPaymentStatus())
                .notes(buildPaymentNotes(request.getNotes(), depositAmount))
                .build();

        return paymentRepository.save(payment);
    }

    private String buildPaymentNotes(String originalNotes, BigDecimal depositAmount) {
        StringBuilder notes = new StringBuilder();
        if (originalNotes != null && !originalNotes.isBlank()) {
            notes.append(originalNotes);
        }
        if (depositAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (notes.length() > 0) {
                notes.append(" | ");
            }
            notes.append(String.format("Đã trừ số tiền cọc: %s VNĐ", formatCurrency(depositAmount)));
        }
        return notes.length() > 0 ? notes.toString() : null;
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0";
        return String.format("%,.0f", amount.doubleValue());
    }
}

