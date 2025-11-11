package com.example.backend_quanlynhahanglau.dto.table;

import com.example.backend_quanlynhahanglau.enums.TableStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableRequest {
    @NotBlank(message = "Số bàn không được để trống")
    private String tableNumber;

    @NotNull(message = "Sức chứa không được để trống")
    @Positive(message = "Sức chứa phải là số dương")
    private Integer capacity;

    private TableStatus status;
    private String location;
    private String notes;
}
