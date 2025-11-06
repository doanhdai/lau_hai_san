package com.example.backend_quanlynhahanglau.dto.order;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
    private Long customerId;
    private Long tableId;
    private Long roomId;
    private Long reservationId;

    @NotEmpty(message = "Đơn hàng phải có ít nhất một món")
    private List<OrderItemRequest> items;

    private String notes;
}
