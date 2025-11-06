package com.example.backend_quanlynhahanglau.dto.order;

import com.example.backend_quanlynhahanglau.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private String customerName;
    private Long customerId;
    private String tableNumber;
    private Long tableId;
    private String roomName;
    private Long roomId;
    private Long reservationId; // ID của reservation nếu đơn hàng được tạo từ đặt bàn trực tuyến
    private List<OrderItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal total;
    private OrderStatus status;
    private String notes;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
