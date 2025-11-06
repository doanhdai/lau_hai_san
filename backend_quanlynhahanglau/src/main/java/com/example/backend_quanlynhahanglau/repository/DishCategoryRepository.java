package com.example.backend_quanlynhahanglau.repository;

import com.example.backend_quanlynhahanglau.entity.DishCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DishCategoryRepository extends JpaRepository<DishCategory, Long> {
    Optional<DishCategory> findByName(String name);
    List<DishCategory> findByActiveTrue();
    Boolean existsByName(String name);
}
