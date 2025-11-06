package com.example.backend_quanlynhahanglau.dto.history;

import com.example.backend_quanlynhahanglau.enums.TableStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableHistoryResponse {
    private Long id;
    private String tableNumber;
    private Long tableId;
    private String roomName;
    private Long roomId;
    private String customerName;
    private Long customerId;
    private String orderNumber;
    private Long orderId;
    private TableStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String notes;
    private LocalDateTime createdAt;
}
