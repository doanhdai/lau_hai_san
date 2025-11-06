package com.example.backend_quanlynhahanglau.dto.reservation;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicReservationRequest {
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không quá 100 ký tự")
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String customerPhone;

    @Email(message = "Email không hợp lệ")
    private String customerEmail;

    @NotNull(message = "Thời gian đặt bàn không được để trống")
    @Future(message = "Thời gian đặt bàn phải trong tương lai")
    private LocalDateTime reservationDateTime;

    @NotNull(message = "Số lượng khách không được để trống")
    @Min(value = 1, message = "Số lượng khách phải ít nhất 1 người")
    @Max(value = 50, message = "Số lượng khách tối đa 50 người")
    private Integer numberOfGuests;

    @NotNull(message = "Bàn không được để trống")
    private Long tableId; // Bắt buộc chọn bàn

    private String notes; // Ghi chú
}
