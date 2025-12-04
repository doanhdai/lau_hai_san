package com.example.backend_quanlynhahanglau.dto.dish;

import com.example.backend_quanlynhahanglau.enums.DishStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishRequest {
    @NotBlank(message = "Tên món ăn không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Giá không được để trống")
    @Positive(message = "Giá phải là số dương")
    private BigDecimal price;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    private DishStatus status;
    private String imageUrl;
    private Boolean isPromotion;
    private Long promotionId;
    
    @Positive(message = "Thời gian dự kiến ra món phải là số dương")
    private Integer estimatedPreparationTime; // Thời gian dự kiến ra món (phút)
}
