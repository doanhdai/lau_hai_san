package com.example.backend_quanlynhahanglau.controller;

import com.example.backend_quanlynhahanglau.dto.ApiResponse;
import com.example.backend_quanlynhahanglau.dto.promotion.PromotionRequest;
import com.example.backend_quanlynhahanglau.dto.promotion.PromotionResponse;
import com.example.backend_quanlynhahanglau.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class PromotionController {
    private final PromotionService promotionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getAllPromotions() {
        List<PromotionResponse> promotions = promotionService.getAllPromotions();
        return ResponseEntity.ok(ApiResponse.success(promotions));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getActivePromotions() {
        List<PromotionResponse> promotions = promotionService.getActivePromotions();
        return ResponseEntity.ok(ApiResponse.success(promotions));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PromotionResponse>> getPromotionById(@PathVariable Long id) {
        PromotionResponse promotion = promotionService.getPromotionById(id);
        return ResponseEntity.ok(ApiResponse.success(promotion));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PromotionResponse>> createPromotion(@Valid @RequestBody PromotionRequest request) {
        PromotionResponse promotion = promotionService.createPromotion(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo khuyến mãi thành công", promotion));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PromotionResponse>> updatePromotion(
            @PathVariable Long id,
            @Valid @RequestBody PromotionRequest request) {
        PromotionResponse promotion = promotionService.updatePromotion(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật khuyến mãi thành công", promotion));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa khuyến mãi thành công", null));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PromotionResponse>> activatePromotion(@PathVariable Long id) {
        promotionService.activatePromotion(id);
        PromotionResponse promotion = promotionService.getPromotionById(id);
        return ResponseEntity.ok(ApiResponse.success("Kích hoạt khuyến mãi thành công", promotion));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PromotionResponse>> deactivatePromotion(@PathVariable Long id) {
        promotionService.deactivatePromotion(id);
        PromotionResponse promotion = promotionService.getPromotionById(id);
        return ResponseEntity.ok(ApiResponse.success("Vô hiệu hóa khuyến mãi thành công", promotion));
    }
}
