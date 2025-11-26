package com.example.backend_quanlynhahanglau.dto.order;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private Long id;
    private String dishName;
    private Long dishId;
    private String dishImageUrl;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
    private String notes;
    private LocalDateTime createdAt;
}
