package com.example.backend_quanlynhahanglau.dto.payment;

import com.example.backend_quanlynhahanglau.enums.PaymentMethod;
import com.example.backend_quanlynhahanglau.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    @NotNull(message = "ID đơn hàng không được để trống")
    private Long orderId;

    @NotNull(message = "Số tiền không được để trống")
    @Positive(message = "Số tiền phải là số dương")
    private BigDecimal amount;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Trạng thái thanh toán không được để trống")
    private PaymentStatus paymentStatus;

    private String notes;
}

