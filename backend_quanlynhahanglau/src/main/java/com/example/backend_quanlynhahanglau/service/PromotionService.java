package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.dto.promotion.PromotionRequest;
import com.example.backend_quanlynhahanglau.dto.promotion.PromotionResponse;
import com.example.backend_quanlynhahanglau.entity.Promotion;
import com.example.backend_quanlynhahanglau.exception.ResourceNotFoundException;
import com.example.backend_quanlynhahanglau.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionService {
    private final PromotionRepository promotionRepository;

    @Transactional
    public List<PromotionResponse> getAllPromotions() {
        List<Promotion> promotions = promotionRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        
        // Auto-update active status based on date range
        for (Promotion promotion : promotions) {
            Integer currentActive = promotion.getActive();
            if (currentActive == null) {
                currentActive = 1; // Default to active
            }
            
            // Only auto-update if active is 1 (active) or 0 (expired)
            // Don't auto-update if active is 2 (manually disabled)
            if (currentActive != 2) {
                if (now.isAfter(promotion.getEndDate())) {
                    // Đã hết hạn -> chuyển sang ngừng hoạt động (0)
                    if (currentActive != 0) {
                        promotion.setActive(0);
                        promotionRepository.save(promotion);
                    }
                } else if (now.isBefore(promotion.getStartDate())) {
                    // Chưa đến ngày bắt đầu -> set active = 1 nếu chưa phải
                    if (currentActive != 1) {
                        promotion.setActive(1);
                        promotionRepository.save(promotion);
                    }
                } else {
                    // Đang trong khoảng thời gian -> set active = 1
                    if (currentActive != 1) {
                        promotion.setActive(1);
                        promotionRepository.save(promotion);
                    }
                }
            }
        }
        
        return promotions.stream()
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
        // Mặc định active = 1 (hoạt động) khi tạo mới
        Promotion promotion = Promotion.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .discountPercent(request.getDiscountPercent())
                .discountAmount(request.getDiscountAmount())
                .minOrderValue(request.getMinOrderValue())
                .maxDiscount(request.getMaxDiscount())
                .active(1) // Mặc định hoạt động
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
        promotion.setMinOrderValue(request.getMinOrderValue());
        promotion.setMaxDiscount(request.getMaxDiscount());
        
        // Update active status based on date range after updating dates
        LocalDateTime now = LocalDateTime.now();
        Integer currentActive = promotion.getActive();
        if (currentActive == null) {
            currentActive = 1;
        }
        
        // Only auto-update if not manually disabled (status != 2)
        if (currentActive != 2) {
            if (now.isAfter(request.getEndDate())) {
                promotion.setActive(0); // Expired
            } else if (now.isBefore(request.getStartDate())) {
                promotion.setActive(1); // Not started yet
            } else {
                promotion.setActive(1); // Active
            }
        }

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
    public void activatePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khuyến mãi", "id", id));
        LocalDateTime now = LocalDateTime.now();
        // Chỉ set active = 1 nếu đang trong khoảng thời gian
        if ((now.isAfter(promotion.getStartDate()) || now.isEqual(promotion.getStartDate())) &&
            (now.isBefore(promotion.getEndDate()) || now.isEqual(promotion.getEndDate()))) {
            promotion.setActive(1);
        } else {
            // Nếu ngoài khoảng thời gian, vẫn set = 1 (sẽ được auto-update sau)
            promotion.setActive(1);
        }
        promotionRepository.save(promotion);
    }

    @Transactional
    public void deactivatePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khuyến mãi", "id", id));
        promotion.setActive(2); // Tắt (manually disabled)
        promotionRepository.save(promotion);
    }

    private PromotionResponse mapToResponse(Promotion promotion) {
        // Determine discount type and value
        String discountType = null;
        BigDecimal discountValue = null;
        
        if (promotion.getDiscountPercent() != null && promotion.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
            discountType = "PERCENTAGE";
            discountValue = promotion.getDiscountPercent();
        } else if (promotion.getDiscountAmount() != null && promotion.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            discountType = "FIXED";
            discountValue = promotion.getDiscountAmount();
        }
        
        // Convert Integer active to Boolean for backward compatibility
        Boolean activeBoolean = (promotion.getActive() != null && promotion.getActive() == 1);
        
        return PromotionResponse.builder()
                .id(promotion.getId())
                .name(promotion.getName())
                .promotionName(promotion.getName()) // Alias for frontend compatibility
                .description(promotion.getDescription())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .discountPercent(promotion.getDiscountPercent())
                .discountAmount(promotion.getDiscountAmount())
                .minOrderValue(promotion.getMinOrderValue())
                .maxDiscount(promotion.getMaxDiscount())
                .active(activeBoolean)
                .status(promotion.getActive() != null ? promotion.getActive() : 1)
                .createdAt(promotion.getCreatedAt())
                .discountType(discountType)
                .discountValue(discountValue)
                .build();
    }
}
