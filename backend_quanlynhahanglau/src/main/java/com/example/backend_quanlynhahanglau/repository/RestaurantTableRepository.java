package com.example.backend_quanlynhahanglau.repository;

import com.example.backend_quanlynhahanglau.entity.RestaurantTable;
import com.example.backend_quanlynhahanglau.enums.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    Optional<RestaurantTable> findByTableNumber(String tableNumber);
    List<RestaurantTable> findByStatus(TableStatus status);
    List<RestaurantTable> findByActiveTrue();
    Boolean existsByTableNumber(String tableNumber);
    
    // Tìm bàn phù hợp theo số người (chỉ lấy bàn chưa bị xóa)
    @Query("SELECT t FROM RestaurantTable t WHERE t.capacity >= :numberOfGuests " +
           "AND t.status = 'AVAILABLE' AND t.active = true " +
           "AND (t.isDeleted IS NULL OR t.isDeleted = false) " +
           "ORDER BY t.capacity ASC")
    List<RestaurantTable> findSuitableTable(@Param("numberOfGuests") Integer numberOfGuests);
    
    // Thống kê bàn theo trạng thái
    @Query("SELECT t.status, COUNT(t) FROM RestaurantTable t GROUP BY t.status")
    List<Object[]> countByStatus();
}
