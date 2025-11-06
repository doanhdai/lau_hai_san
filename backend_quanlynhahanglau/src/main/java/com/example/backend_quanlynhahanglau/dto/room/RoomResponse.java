package com.example.backend_quanlynhahanglau.dto.room;

import com.example.backend_quanlynhahanglau.enums.TableStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {
    private Long id;
    private String roomNumber;
    private String name;
    private Integer capacity;
    private TableStatus status;
    private String description;
    private Boolean active;
}
