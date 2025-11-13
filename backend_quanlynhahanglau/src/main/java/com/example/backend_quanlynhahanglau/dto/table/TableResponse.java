package com.example.backend_quanlynhahanglau.dto.table;

import com.example.backend_quanlynhahanglau.enums.TableStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableResponse {
    private Long id;
    private String tableNumber;
    private Integer capacity;
    private TableStatus status;
    private String location;
    private Boolean active;
    private String notes;
    private Integer positionX; // Vị trí X trên bản đồ (pixels)
    private Integer positionY; // Vị trí Y trên bản đồ (pixels)
}
