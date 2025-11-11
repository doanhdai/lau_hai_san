package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.dto.category.CategoryRequest;
import com.example.backend_quanlynhahanglau.dto.category.CategoryResponse;
import com.example.backend_quanlynhahanglau.entity.DishCategory;
import com.example.backend_quanlynhahanglau.exception.DuplicateResourceException;
import com.example.backend_quanlynhahanglau.exception.ResourceNotFoundException;
import com.example.backend_quanlynhahanglau.repository.DishCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DishCategoryService {
    private final DishCategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        DishCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", id));
        return mapToResponse(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Danh mục", "tên", request.getName());
        }

        DishCategory category = DishCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .build();

        category = categoryRepository.save(category);
        return mapToResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        DishCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", id));

        if (!category.getName().equals(request.getName()) 
                && categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Danh mục", "tên", request.getName());
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        
        // Cập nhật trạng thái active nếu được cung cấp
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }

        category = categoryRepository.save(category);
        return mapToResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        DishCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", id));
        categoryRepository.delete(category);
    }

    @Transactional
    public CategoryResponse toggleCategoryStatus(Long id) {
        DishCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", id));
        category.setActive(!category.getActive());
        DishCategory updatedCategory = categoryRepository.save(category);
        return mapToResponse(updatedCategory);
    }

    private CategoryResponse mapToResponse(DishCategory category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.getActive())
                .build();
    }
}
