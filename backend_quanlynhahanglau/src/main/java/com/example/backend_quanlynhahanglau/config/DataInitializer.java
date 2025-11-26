package com.example.backend_quanlynhahanglau.config;

import com.example.backend_quanlynhahanglau.entity.Role;
import com.example.backend_quanlynhahanglau.entity.User;
import com.example.backend_quanlynhahanglau.enums.RoleName;
import com.example.backend_quanlynhahanglau.repository.CustomerRepository;
import com.example.backend_quanlynhahanglau.repository.RoleRepository;
import com.example.backend_quanlynhahanglau.repository.UserRepository;
import com.example.backend_quanlynhahanglau.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
            
        initializeDefaultAdmin();
        
        generateCustomerCodes();
        
    }

    private void initializeRoles() {
        log.info("Initializing roles...");
        
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = Role.builder()
                        .name(roleName)
                        .description(getRoleDescription(roleName))
                        .build();
                roleRepository.save(role);
                log.info("Created role: {}", roleName);
            } else {
                log.info("Role already exists: {}", roleName);
            }
        }
    }

    private void initializeDefaultAdmin() {
        log.info("Initializing default admin user...");
        
        String adminUsername = "admin";
        
        if (!userRepository.existsByUsername(adminUsername)) {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
            
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            
            User admin = User.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@nhahang.com")
                    .fullName("Administrator")
                    .phone("0123456789")
                    .active(true)
                    .roles(roles)
                    .build();
            
            userRepository.save(admin);
            log.info("Created default admin user: {}", adminUsername);
        } else {
            log.info("Admin user already exists: {}", adminUsername);
        }
    }

    private void generateCustomerCodes() {
        log.info("Generating customer codes for existing customers...");
        
        customerRepository.findAll().forEach(customer -> {
            if (customer.getCustomerCode() == null || customer.getCustomerCode().isEmpty()) {
                String code;
                do {
                    code = CodeGenerator.generateCustomerCode();
                } while (customerRepository.existsByCustomerCode(code));
                
                customer.setCustomerCode(code);
                customerRepository.save(customer);
                log.info("Generated customer code: {} for customer: {}", code, customer.getFullName());
            }
        });
    }

    private String getRoleDescription(RoleName roleName) {
        switch (roleName) {
            case ROLE_ADMIN:
                return "Quản trị viên hệ thống - Quyền cao nhất";
            case ROLE_MANAGER:
                return "Quản lý nhà hàng - Quản lý hoạt động";
            case ROLE_STAFF:
                return "Nhân viên - Phục vụ và xử lý đơn";
            case ROLE_CUSTOMER:
                return "Khách hàng - Đặt bàn và đánh giá";
            default:
                return "Unknown role";
        }
    }
}
