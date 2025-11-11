package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.dto.dish.DishRequest;
import com.example.backend_quanlynhahanglau.dto.dish.DishResponse;
import com.example.backend_quanlynhahanglau.entity.Dish;
import com.example.backend_quanlynhahanglau.entity.DishCategory;
import com.example.backend_quanlynhahanglau.entity.Promotion;
import com.example.backend_quanlynhahanglau.enums.DishStatus;
import com.example.backend_quanlynhahanglau.exception.ResourceNotFoundException;
import com.example.backend_quanlynhahanglau.repository.DishCategoryRepository;
import com.example.backend_quanlynhahanglau.repository.DishRepository;
import com.example.backend_quanlynhahanglau.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DishService {
    private final DishRepository dishRepository;
    private final DishCategoryRepository categoryRepository;
    private final PromotionRepository promotionRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public List<DishResponse> getAllDishes() {
        // Filter thủ công để chỉ lấy món active
        return dishRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DishResponse> getAvailableDishes() {
        // Filter thủ công để chỉ lấy món active và available
        return dishRepository.findAll().stream()
                .filter(dish -> dish.getActive() && dish.getStatus() == DishStatus.AVAILABLE)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DishResponse getDishById(Long id) {
        // Filter thủ công: chỉ trả về món active
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn", "id", id));
        
        if (!dish.getActive()) {
            throw new ResourceNotFoundException("Món ăn", "id", id);
        }
        
        return mapToResponse(dish);
    }

    @Transactional(readOnly = true)
    public List<DishResponse> searchDishes(String keyword) {
        // Filter thủ công để chỉ lấy món active
        return dishRepository.searchByNameOrCategory(keyword).stream()
                .filter(dish -> dish.getActive())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DishResponse> getDishesByCategoryId(Long categoryId) {
        // Kiểm tra category có tồn tại không
        DishCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", categoryId));
        
        // Lấy tất cả món của category và filter chỉ lấy món active
        return dishRepository.findByCategory(category).stream()
                .filter(dish -> dish.getActive())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DishResponse createDish(DishRequest request) {
        DishCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", request.getCategoryId()));

        Dish dish = Dish.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(category)
                .status(request.getStatus() != null ? request.getStatus() : DishStatus.AVAILABLE)
                .imageUrl(request.getImageUrl())
                .isPromotion(request.getIsPromotion() != null ? request.getIsPromotion() : false)
                .active(true)
                .build();

        if (request.getPromotionId() != null) {
            Promotion promotion = promotionRepository.findById(request.getPromotionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Khuyến mãi", "id", request.getPromotionId()));
            dish.setPromotion(promotion);
        }

        dish = dishRepository.save(dish);
        return mapToResponse(dish);
    }

    @Transactional
    public DishResponse createDishWithImage(DishRequest request, MultipartFile imageFile) throws IOException {
        DishCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", request.getCategoryId()));

        // Xử lý ảnh: nếu có file ảnh thì upload, nếu không thì dùng imageUrl từ request
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = cloudinaryService.uploadImage(imageFile, "dishes");
        } else if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            imageUrl = request.getImageUrl();
        }

        Dish dish = Dish.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(category)
                .status(request.getStatus() != null ? request.getStatus() : DishStatus.AVAILABLE)
                .imageUrl(imageUrl)
                .isPromotion(request.getIsPromotion() != null ? request.getIsPromotion() : false)
                .active(true)
                .build();

        if (request.getPromotionId() != null) {
            Promotion promotion = promotionRepository.findById(request.getPromotionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Khuyến mãi", "id", request.getPromotionId()));
            dish.setPromotion(promotion);
        }

        dish = dishRepository.save(dish);
        return mapToResponse(dish);
    }

    @Transactional
    public DishResponse updateDish(Long id, DishRequest request) {
        // Filter thủ công: chỉ cho phép update món active
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn", "id", id));
        
        if (!dish.getActive()) {
            throw new ResourceNotFoundException("Món ăn", "id", id);
        }

        DishCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", request.getCategoryId()));

        dish.setName(request.getName());
        dish.setDescription(request.getDescription());
        dish.setPrice(request.getPrice());
        dish.setCategory(category);
        dish.setStatus(request.getStatus());
        dish.setImageUrl(request.getImageUrl());
        dish.setIsPromotion(request.getIsPromotion());

        if (request.getPromotionId() != null) {
            Promotion promotion = promotionRepository.findById(request.getPromotionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Khuyến mãi", "id", request.getPromotionId()));
            dish.setPromotion(promotion);
        } else {
            dish.setPromotion(null);
        }

        dish = dishRepository.save(dish);
        return mapToResponse(dish);
    }

    @Transactional
    public void deleteDish(Long id) {
        // Kiểm tra món ăn có tồn tại và đang active không
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn", "id", id));
        
        if (!dish.getActive()) {
            throw new ResourceNotFoundException("Món ăn", "id", id);
        }
        
        // Soft delete: sử dụng @Modifying query để chắc chắn chỉ chạy UPDATE
        dishRepository.softDeleteById(id);
    }

    @Transactional
    public void updateDishStatus(Long id, DishStatus status) {
        // Filter thủ công: chỉ cho phép update món active
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn", "id", id));
        
        if (!dish.getActive()) {
            throw new ResourceNotFoundException("Món ăn", "id", id);
        }
        
        dish.setStatus(status);
        dishRepository.save(dish);
    }

    @Transactional
    public DishResponse updateDishImage(Long id, MultipartFile imageFile) throws IOException {
        // Kiểm tra món ăn có tồn tại và đang active không
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn", "id", id));
        
        if (!dish.getActive()) {
            throw new ResourceNotFoundException("Món ăn", "id", id);
        }

        // Xóa ảnh cũ nếu có
        if (dish.getImageUrl() != null && !dish.getImageUrl().isEmpty()) {
            cloudinaryService.deleteImage(dish.getImageUrl());
        }

        // Upload ảnh mới lên Cloudinary
        String newImageUrl = cloudinaryService.uploadImage(imageFile, "dishes");
        dish.setImageUrl(newImageUrl);
        dish = dishRepository.save(dish);

        return mapToResponse(dish);
    }

    @Transactional
    public DishResponse updateDishWithImage(Long id, DishRequest request, MultipartFile imageFile) throws IOException {
        // Kiểm tra món ăn có tồn tại và đang active không
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn", "id", id));
        
        if (!dish.getActive()) {
            throw new ResourceNotFoundException("Món ăn", "id", id);
        }

        DishCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", request.getCategoryId()));

        // Cập nhật thông tin món ăn
        dish.setName(request.getName());
        dish.setDescription(request.getDescription());
        dish.setPrice(request.getPrice());
        dish.setCategory(category);
        dish.setStatus(request.getStatus() != null ? request.getStatus() : dish.getStatus());
        dish.setIsPromotion(request.getIsPromotion() != null ? request.getIsPromotion() : dish.getIsPromotion());

        // Xử lý ảnh: nếu có file ảnh mới thì upload, nếu không thì giữ nguyên hoặc dùng imageUrl từ request
        if (imageFile != null && !imageFile.isEmpty()) {
            // Xóa ảnh cũ nếu có
            if (dish.getImageUrl() != null && !dish.getImageUrl().isEmpty()) {
                cloudinaryService.deleteImage(dish.getImageUrl());
            }
            // Upload ảnh mới lên Cloudinary
            String newImageUrl = cloudinaryService.uploadImage(imageFile, "dishes");
            dish.setImageUrl(newImageUrl);
        } else if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            // Nếu không có file nhưng có imageUrl trong request, dùng imageUrl đó
            dish.setImageUrl(request.getImageUrl());
        }
        // Nếu cả 2 đều null/empty thì giữ nguyên ảnh cũ

        // Xử lý promotion
        if (request.getPromotionId() != null) {
            Promotion promotion = promotionRepository.findById(request.getPromotionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Khuyến mãi", "id", request.getPromotionId()));
            dish.setPromotion(promotion);
        } else {
            dish.setPromotion(null);
        }

        dish = dishRepository.save(dish);
        return mapToResponse(dish);
    }

    private DishResponse mapToResponse(Dish dish) {
        return DishResponse.builder()
                .id(dish.getId())
                .name(dish.getName())
                .description(dish.getDescription())
                .price(dish.getPrice())
                .categoryName(dish.getCategory().getName())
                .categoryId(dish.getCategory().getId())
                .status(dish.getStatus())
                .imageUrl(dish.getImageUrl())
                .isPromotion(dish.getIsPromotion())
                .promotionName(dish.getPromotion() != null ? dish.getPromotion().getName() : null)
                .active(dish.getActive())
                .build();
    }
}
