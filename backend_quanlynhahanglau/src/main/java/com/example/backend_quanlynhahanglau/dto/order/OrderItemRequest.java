package com.example.backend_quanlynhahanglau.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {
    @NotNull(message = "Món ăn không được để trống")
    private Long dishId;

    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải là số dương")
    private Integer quantity;

    private String notes;
}
