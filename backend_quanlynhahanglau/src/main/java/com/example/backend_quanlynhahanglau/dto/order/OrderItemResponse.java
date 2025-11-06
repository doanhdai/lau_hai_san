package com.example.backend_quanlynhahanglau.dto.order;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private Long id;
    private String dishName;
    private Long dishId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
    private String notes;
}
