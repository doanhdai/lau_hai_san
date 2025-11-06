package com.example.backend_quanlynhahanglau.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20)
    private String customerCode; // Mã khách hàng: C-XXXXXX (auto-generated)

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(unique = true, nullable = false, length = 15)
    private String phone;

    @Column(unique = true, length = 100)
    private String email;

    @Column(length = 500)
    private String address;

    @Column(columnDefinition = "bit default 1")
    private Boolean isVip = false;

    @Column(columnDefinition = "bit default 1")
    private Boolean active = true;

    @Column(columnDefinition = "bit default 0")
    private Boolean blocked = false;

    @Column(length = 500)
    private String notes; // Ghi chú về khách hàng

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
