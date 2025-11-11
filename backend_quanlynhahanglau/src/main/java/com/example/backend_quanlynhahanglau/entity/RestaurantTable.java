package com.example.backend_quanlynhahanglau.entity;

import com.example.backend_quanlynhahanglau.enums.TableStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurant_tables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String tableNumber;

    @Column(nullable = false)
    private Integer capacity; // Số chỗ ngồi

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TableStatus status = TableStatus.AVAILABLE;

    @Column(length = 100)
    private String location; // Vị trí trong nhà hàng

    @Column(columnDefinition = "bit default 1")
    private Boolean active = true;

    @Column(length = 500)
    private String notes;
}
