package com.example.backend_quanlynhahanglau.dto.customer;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private Boolean isVip;
    private Boolean active;
    private Boolean blocked;
    private String notes;
    private LocalDateTime createdAt;
}
