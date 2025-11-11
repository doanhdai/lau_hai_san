package com.example.backend_quanlynhahanglau.repository;

import com.example.backend_quanlynhahanglau.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByPhone(String phone);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByCustomerCode(String customerCode);
    Boolean existsByPhone(String phone);
    Boolean existsByEmail(String email);
    Boolean existsByCustomerCode(String customerCode);
    
    List<Customer> findByIsVipTrue();
    List<Customer> findByActiveTrue();
    List<Customer> findByBlockedTrue();
    
    // Tìm top khách hàng đặt bàn nhiều nhất
    @Query("SELECT c FROM Customer c JOIN Reservation r ON r.customer = c " +
           "GROUP BY c ORDER BY COUNT(r) DESC")
    List<Customer> findTopCustomers();
    
    // Tìm Customer theo User ID (có thể có nhiều Customer cho 1 User)
    List<Customer> findByUserId(Long userId);
}
