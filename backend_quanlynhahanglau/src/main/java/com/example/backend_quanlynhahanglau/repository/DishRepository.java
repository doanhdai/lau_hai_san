package com.example.backend_quanlynhahanglau.repository;

import com.example.backend_quanlynhahanglau.entity.Dish;
import com.example.backend_quanlynhahanglau.entity.DishCategory;
import com.example.backend_quanlynhahanglau.enums.DishStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
    List<Dish> findByCategory(DishCategory category);
    List<Dish> findByStatus(DishStatus status);
    List<Dish> findByActiveTrue();
    List<Dish> findByIsPromotionTrue();
    
    // Soft delete: set active = false (chắc chắn chỉ chạy UPDATE, không DELETE)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Dish d SET d.active = false WHERE d.id = :id")
    int softDeleteById(@Param("id") Long id);
    
    // @Where tự động filter active = true, không cần thêm điều kiện active trong query
    // Tìm kiếm món ăn theo tên hoặc nhóm
    @Query("SELECT d FROM Dish d WHERE " +
           "LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(d.category.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Dish> searchByNameOrCategory(@Param("keyword") String keyword);
    
    // Thống kê số lượng món ăn đã bán
    // Lưu ý: @Where không áp dụng cho native query, nên cần filter thủ công
    @Query("SELECT d, SUM(od.quantity) as totalSold FROM Dish d " +
           "JOIN OrderDetail od ON od.dish = d " +
           "WHERE d.active = true " +
           "GROUP BY d ORDER BY totalSold DESC")
    List<Object[]> findBestSellingDishes();
}
