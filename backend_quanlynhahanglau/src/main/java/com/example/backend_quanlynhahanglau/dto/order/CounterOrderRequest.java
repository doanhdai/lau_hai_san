package com.example.backend_quanlynhahanglau.dto.order;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CounterOrderRequest {
    @NotNull(message = "Vui lòng chọn bàn")
    private Long tableId; // Bàn cho khách đặt trực tiếp tại nhà hàng (bắt buộc)
    
    @NotEmpty(message = "Đơn hàng phải có ít nhất một món")
    private List<OrderItemRequest> items;
    
    private String notes;
    
    private Long promotionId; // Optional promotion code
}
