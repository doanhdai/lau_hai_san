package com.example.backend_quanlynhahanglau.dto.dish;

import com.example.backend_quanlynhahanglau.enums.DishStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String categoryName;
    private Long categoryId;
    private DishStatus status;
    private String imageUrl;
    private Boolean isPromotion;
    private String promotionName;
    private Boolean active;
    private LocalDateTime createdAt;
    private Integer estimatedPreparationTime; // Thời gian dự kiến ra món (phút)
}
