package com.example.backend_quanlynhahanglau.config.controller;

import com.example.backend_quanlynhahanglau.dto.ApiResponse;
import com.example.backend_quanlynhahanglau.dto.dish.DishRequest;
import com.example.backend_quanlynhahanglau.dto.dish.DishResponse;
import com.example.backend_quanlynhahanglau.enums.DishStatus;
import com.example.backend_quanlynhahanglau.exception.BadRequestException;
import com.example.backend_quanlynhahanglau.exception.ResourceNotFoundException;
import com.example.backend_quanlynhahanglau.service.DishService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/dishes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class DishController {
    private final DishService dishService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DishResponse>>> getAllDishes() {
        List<DishResponse> dishes = dishService.getAllDishes();
        return ResponseEntity.ok(ApiResponse.success(dishes));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<DishResponse>>> getAvailableDishes() {
        List<DishResponse> dishes = dishService.getAvailableDishes();
        return ResponseEntity.ok(ApiResponse.success(dishes));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<DishResponse>>> searchDishes(@RequestParam String keyword) {
        List<DishResponse> dishes = dishService.searchDishes(keyword);
        return ResponseEntity.ok(ApiResponse.success(dishes));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<DishResponse>>> getDishesByCategory(@PathVariable Long categoryId) {
        List<DishResponse> dishes = dishService.getDishesByCategoryId(categoryId);
        return ResponseEntity.ok(ApiResponse.success(dishes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DishResponse>> getDishById(@PathVariable Long id) {
        DishResponse dish = dishService.getDishById(id);
        return ResponseEntity.ok(ApiResponse.success(dish));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<DishResponse>> createDish(@Valid @RequestBody DishRequest request) {
        DishResponse dish = dishService.createDish(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo món ăn thành công", dish));
    }

    @PostMapping(value = "/form", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<DishResponse>> createDishWithForm(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("price") java.math.BigDecimal price,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "status", required = false) String statusStr,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            @RequestParam(value = "isPromotion", required = false) String isPromotionStr,
            @RequestParam(value = "promotionId", required = false) Long promotionId,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {
        try {
            // Parse isPromotion từ string (FormData gửi boolean dưới dạng string)
            Boolean isPromotion = null;
            if (isPromotionStr != null && !isPromotionStr.isEmpty()) {
                isPromotion = Boolean.parseBoolean(isPromotionStr);
            }
            
            // Parse status từ string và validate
            DishStatus status = null;
            if (statusStr != null && !statusStr.trim().isEmpty()) {
                try {
                    status = DishStatus.valueOf(statusStr.toUpperCase().trim());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Trạng thái không hợp lệ. Giá trị hợp lệ: AVAILABLE, UNAVAILABLE, DISCONTINUED"));
                }
            }
            
            // Validate required fields
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Tên món ăn không được để trống"));
            }
            
            if (categoryId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Danh mục không được để trống"));
            }
            
            if (price == null || price.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Giá phải là số dương"));
            }
            
            // Tạo DishRequest từ các tham số
            DishRequest request = DishRequest.builder()
                    .name(name.trim())
                    .description(description != null ? description.trim() : null)
                    .price(price)
                    .categoryId(categoryId)
                    .status(status)
                    .imageUrl(imageUrl != null && !imageUrl.isEmpty() ? imageUrl : null)
                    .isPromotion(isPromotion)
                    .promotionId(promotionId)
                    .build();

            // Validate file nếu có
            if (imageFile != null && !imageFile.isEmpty()) {
                String contentType = imageFile.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("File phải là ảnh (jpg, jpeg, png, gif, webp)"));
                }
            }

            DishResponse dish = dishService.createDishWithImage(request, imageFile);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Tạo món ăn thành công", dish));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi upload ảnh: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi tạo món ăn: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<DishResponse>> updateDish(
            @PathVariable Long id,
            @Valid @RequestBody DishRequest request) {
        DishResponse dish = dishService.updateDish(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật món ăn thành công", dish));
    }

    @PutMapping(value = "/{id}/form", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<DishResponse>> updateDishWithForm(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("price") java.math.BigDecimal price,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "status", required = false) String statusStr,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            @RequestParam(value = "isPromotion", required = false) String isPromotionStr,
            @RequestParam(value = "promotionId", required = false) Long promotionId,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {
        try {
            // Parse isPromotion từ string (FormData gửi boolean dưới dạng string)
            Boolean isPromotion = null;
            if (isPromotionStr != null && !isPromotionStr.isEmpty()) {
                isPromotion = Boolean.parseBoolean(isPromotionStr);
            }
            
            // Parse status từ string và validate
            DishStatus status = null;
            if (statusStr != null && !statusStr.trim().isEmpty()) {
                try {
                    status = DishStatus.valueOf(statusStr.toUpperCase().trim());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Trạng thái không hợp lệ. Giá trị hợp lệ: AVAILABLE, UNAVAILABLE, DISCONTINUED"));
                }
            }
            
            // Validate required fields
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Tên món ăn không được để trống"));
            }
            
            if (categoryId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Danh mục không được để trống"));
            }
            
            if (price == null || price.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Giá phải là số dương"));
            }
            
            // Tạo DishRequest từ các tham số
            DishRequest request = DishRequest.builder()
                    .name(name.trim())
                    .description(description != null ? description.trim() : null)
                    .price(price)
                    .categoryId(categoryId)
                    .status(status)
                    .imageUrl(imageUrl != null && !imageUrl.isEmpty() ? imageUrl : null)
                    .isPromotion(isPromotion)
                    .promotionId(promotionId)
                    .build();

            // Validate file nếu có
            if (imageFile != null && !imageFile.isEmpty()) {
                String contentType = imageFile.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("File phải là ảnh (jpg, jpeg, png, gif, webp)"));
                }
            }

            DishResponse dish = dishService.updateDishWithImage(id, request, imageFile);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật món ăn thành công", dish));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi upload ảnh: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi cập nhật món ăn: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDish(@PathVariable Long id) {
        dishService.deleteDish(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa món ăn thành công", null));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<Void>> updateDishStatus(
            @PathVariable Long id,
            @RequestParam DishStatus status) {
        dishService.updateDishStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái món ăn thành công", null));
    }

    @PutMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<DishResponse>> updateDishImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile imageFile) {
        try {
            // Validate file
            if (imageFile.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File ảnh không được để trống"));
            }

            // Validate file type
            String contentType = imageFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File phải là ảnh (jpg, jpeg, png, gif, webp)"));
            }

            DishResponse dish = dishService.updateDishImage(id, imageFile);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật ảnh món ăn thành công", dish));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi upload ảnh: " + e.getMessage()));
        }
    }
}
