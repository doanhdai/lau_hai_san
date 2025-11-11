package com.example.backend_quanlynhahanglau.dto.category;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {
    @NotBlank(message = "Tên danh mục không được để trống")
    @JsonAlias("categoryName") // Hỗ trợ cả "name" và "categoryName" từ frontend
    private String name;

    private String description;
    
    private Boolean active;
}
