package com.example.backend_quanlynhahanglau.repository;

import com.example.backend_quanlynhahanglau.entity.RestaurantTable;
import com.example.backend_quanlynhahanglau.entity.TableHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TableHistoryRepository extends JpaRepository<TableHistory, Long> {
    List<TableHistory> findByTable(RestaurantTable table);
    
    // Lịch sử sử dụng bàn theo thời gian
    @Query("SELECT th FROM TableHistory th WHERE th.table = :table " +
           "AND th.startTime >= :startDate AND th.startTime <= :endDate")
    List<TableHistory> findByTableAndDateRange(
        @Param("table") RestaurantTable table,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Báo cáo sử dụng bàn theo thời gian
    @Query("SELECT th FROM TableHistory th WHERE " +
           "th.startTime >= :startDate AND th.startTime <= :endDate")
    List<TableHistory> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
