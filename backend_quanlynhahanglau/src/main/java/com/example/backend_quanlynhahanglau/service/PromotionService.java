package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.dto.promotion.PromotionRequest;
import com.example.backend_quanlynhahanglau.dto.promotion.PromotionResponse;
import com.example.backend_quanlynhahanglau.entity.Promotion;
import com.example.backend_quanlynhahanglau.exception.ResourceNotFoundException;
import com.example.backend_quanlynhahanglau.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionService {
    private final PromotionRepository promotionRepository;

    @Transactional(readOnly = true)
    public List<PromotionResponse> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PromotionResponse> getActivePromotions() {
        return promotionRepository.findActivePromotions(LocalDateTime.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PromotionResponse getPromotionById(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khuyến mãi", "id", id));
        return mapToResponse(promotion);
    }

    @Transactional
    public PromotionResponse createPromotion(PromotionRequest request) {
        Promotion promotion = Promotion.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .discountPercent(request.getDiscountPercent())
                .discountAmount(request.getDiscountAmount())
                .active(true)
                .build();

        promotion = promotionRepository.save(promotion);
        return mapToResponse(promotion);
    }

    @Transactional
    public PromotionResponse updatePromotion(Long id, PromotionRequest request) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khuyến mãi", "id", id));

        promotion.setName(request.getName());
        promotion.setDescription(request.getDescription());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setDiscountPercent(request.getDiscountPercent());
        promotion.setDiscountAmount(request.getDiscountAmount());

        promotion = promotionRepository.save(promotion);
        return mapToResponse(promotion);
    }

    @Transactional
    public void deletePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khuyến mãi", "id", id));
        promotionRepository.delete(promotion);
    }

    @Transactional
    public void deactivatePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khuyến mãi", "id", id));
        promotion.setActive(false);
        promotionRepository.save(promotion);
    }

    private PromotionResponse mapToResponse(Promotion promotion) {
        return PromotionResponse.builder()
                .id(promotion.getId())
                .name(promotion.getName())
                .description(promotion.getDescription())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .discountPercent(promotion.getDiscountPercent())
                .discountAmount(promotion.getDiscountAmount())
                .active(promotion.getActive())
                .createdAt(promotion.getCreatedAt())
                .build();
    }
}
