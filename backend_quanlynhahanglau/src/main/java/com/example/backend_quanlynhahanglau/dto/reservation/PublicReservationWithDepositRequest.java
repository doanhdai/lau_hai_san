package com.example.backend_quanlynhahanglau.dto.reservation;

import com.example.backend_quanlynhahanglau.dto.order.OrderItemRequest;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicReservationWithDepositRequest {
    @NotBlank(message = "Tên khách hàng không được để trống")
    @Size(max = 100, message = "Tên khách hàng không được vượt quá 100 ký tự")
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String customerPhone;

    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String customerEmail;

    @NotNull(message = "Thời gian đặt bàn không được để trống")
    @Future(message = "Thời gian đặt bàn phải là thời gian trong tương lai")
    private LocalDateTime reservationDateTime;

    @NotNull(message = "Số lượng khách không được để trống")
    @Min(value = 1, message = "Số lượng khách phải lớn hơn 0")
    @Max(value = 50, message = "Số lượng khách không được vượt quá 50")
    private Integer numberOfGuests;

    private Long tableId;
    private String notes;
    private Long userId;

    @NotNull(message = "Danh sách món không được để trống")
    @NotEmpty(message = "Phải có ít nhất một món")
    private List<OrderItemRequest> items;

    @NotNull(message = "Số tiền cọc không được để trống")
    @Positive(message = "Số tiền cọc phải là số dương")
    private BigDecimal depositAmount;
}

