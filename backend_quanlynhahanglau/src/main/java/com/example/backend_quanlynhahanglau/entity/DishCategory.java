package com.example.backend_quanlynhahanglau.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dish_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name; // Lẩu, Hải sản, Khai vị, Nước uống, etc.

    @Column(length = 500)
    private String description;

    @Column(columnDefinition = "bit default 1")
    private Boolean active = true;
}
