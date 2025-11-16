package com.example.backend_quanlynhahanglau.dto.order;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
    private Long customerId;
    private Long tableId;
    private Long reservationId;

    private List<OrderItemRequest> items; // Có thể null khi chỉ cập nhật discount/total

    private String notes;
    
    // Các field này dùng cho update order
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal total;
}
