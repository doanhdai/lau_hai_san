package com.example.backend_quanlynhahanglau.dto.feedback;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackRequest {
    @NotNull(message = "Khách hàng không được để trống")
    private Long customerId;

    private Long orderId;

    @NotNull(message = "Đánh giá không được để trống")
    @Min(value = 1, message = "Đánh giá phải từ 1-5 sao")
    @Max(value = 5, message = "Đánh giá phải từ 1-5 sao")
    private Integer rating;

    private String comment;
}
