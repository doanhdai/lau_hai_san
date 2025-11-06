package com.example.backend_quanlynhahanglau.dto.feedback;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackResponse {
    private Long id;
    private String customerName;
    private Long customerId;
    private String orderNumber;
    private Long orderId;
    private Integer rating;
    private String comment;
    private Boolean resolved;
    private String resolvedByName;
    private String response;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
