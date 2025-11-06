package com.example.backend_quanlynhahanglau.repository;

import com.example.backend_quanlynhahanglau.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    
    // Search users by name, email, or phone
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);
    
    // Find users by active status
    Page<User> findByActive(Boolean active, Pageable pageable);
    
    // Find users by role name
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findByRoleName(@Param("roleName") com.example.backend_quanlynhahanglau.enums.RoleName roleName, Pageable pageable);
    
    // Combined search with filters
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN u.roles r WHERE " +
           "(:search IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:active IS NULL OR u.active = :active) AND " +
           "(:roleName IS NULL OR r.name = :roleName)")
    Page<User> findWithFilters(@Param("search") String search, 
                               @Param("active") Boolean active,
                               @Param("roleName") com.example.backend_quanlynhahanglau.enums.RoleName roleName,
                               Pageable pageable);
    
    // Count users by role
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Long countByRoleName(@Param("roleName") com.example.backend_quanlynhahanglau.enums.RoleName roleName);
}
