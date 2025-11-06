package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.dto.auth.JwtResponse;
import com.example.backend_quanlynhahanglau.dto.auth.LoginRequest;
import com.example.backend_quanlynhahanglau.dto.auth.RegisterRequest;
import com.example.backend_quanlynhahanglau.entity.Role;
import com.example.backend_quanlynhahanglau.entity.User;
import com.example.backend_quanlynhahanglau.enums.RoleName;
import com.example.backend_quanlynhahanglau.exception.BadRequestException;
import com.example.backend_quanlynhahanglau.exception.DuplicateResourceException;
import com.example.backend_quanlynhahanglau.repository.RoleRepository;
import com.example.backend_quanlynhahanglau.repository.UserRepository;
import com.example.backend_quanlynhahanglau.security.JwtUtils;
import com.example.backend_quanlynhahanglau.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Transactional
    public JwtResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for username: {}", loginRequest.getUsername());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toSet());

        log.info("User logged in successfully: {} with roles: {}", userDetails.getUsername(), roles);

        return JwtResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .fullName(userDetails.getFullName())
                .roles(roles)
                .build();
    }

    @Transactional
    public String register(RegisterRequest registerRequest) {
        log.info("Register attempt for username: {}", registerRequest.getUsername());
        
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            log.warn("Registration failed: Username already exists - {}", registerRequest.getUsername());
            throw new DuplicateResourceException("Username đã tồn tại!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            log.warn("Registration failed: Email already exists - {}", registerRequest.getEmail());
            throw new DuplicateResourceException("Email đã được sử dụng!");
        }

        Set<String> strRoles = registerRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        // Xác định roles
        if (strRoles == null || strRoles.isEmpty()) {
            // Mặc định: User đăng ký từ web sẽ là CUSTOMER
            log.info("No roles specified, assigning default role: ROLE_CUSTOMER");
            Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                    .orElseThrow(() -> new BadRequestException("Role CUSTOMER không tồn tại. Vui lòng liên hệ quản trị viên!"));
            roles.add(customerRole);
        } else {
            log.info("Assigning roles: {}", strRoles);
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                                .orElseThrow(() -> new BadRequestException("Role ADMIN không tồn tại"));
                        roles.add(adminRole);
                        break;
                    case "manager":
                        Role managerRole = roleRepository.findByName(RoleName.ROLE_MANAGER)
                                .orElseThrow(() -> new BadRequestException("Role MANAGER không tồn tại"));
                        roles.add(managerRole);
                        break;
                    case "staff":
                        Role staffRole = roleRepository.findByName(RoleName.ROLE_STAFF)
                                .orElseThrow(() -> new BadRequestException("Role STAFF không tồn tại"));
                        roles.add(staffRole);
                        break;
                    case "customer":
                    default:
                        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                                .orElseThrow(() -> new BadRequestException("Role CUSTOMER không tồn tại"));
                        roles.add(customerRole);
                        break;
                }
            });
        }

        // Tạo user
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .fullName(registerRequest.getFullName())
                .phone(registerRequest.getPhone())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .active(true)
                .roles(roles)  // Set roles trực tiếp khi build
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {} with roles: {}", 
                savedUser.getUsername(), 
                savedUser.getRoles().stream().map(r -> r.getName().toString()).collect(Collectors.joining(", ")));

        return "Đăng ký thành công!";
    }
}
