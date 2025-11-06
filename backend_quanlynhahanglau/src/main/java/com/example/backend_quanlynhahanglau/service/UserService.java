package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.dto.user.ChangePasswordRequest;
import com.example.backend_quanlynhahanglau.dto.user.UserRequest;
import com.example.backend_quanlynhahanglau.dto.user.UserResponse;
import com.example.backend_quanlynhahanglau.dto.user.UserUpdateRequest;
import com.example.backend_quanlynhahanglau.entity.Role;
import com.example.backend_quanlynhahanglau.entity.User;
import com.example.backend_quanlynhahanglau.enums.RoleName;
import com.example.backend_quanlynhahanglau.exception.ResourceNotFoundException;
import com.example.backend_quanlynhahanglau.repository.RoleRepository;
import com.example.backend_quanlynhahanglau.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(String search, Boolean active, String role, Pageable pageable) {
        RoleName roleName = null;
        if (role != null && !role.isEmpty()) {
            try {
                roleName = RoleName.valueOf(role);
            } catch (IllegalArgumentException e) {
                // Invalid role, ignore
            }
        }

        Page<User> users;
        if (search != null || active != null || roleName != null) {
            users = userRepository.findWithFilters(search, active, roleName, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(UserResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse createUser(UserRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhoneNumber())
                .address(request.getAddress())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        // Set roles
        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                try {
                    RoleName roleEnum = RoleName.valueOf(roleName);
                    Role role = roleRepository.findByName(roleEnum)
                            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò: " + roleName));
                    roles.add(role);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Vai trò không hợp lệ: " + roleName);
                }
            }
        } else {
            // Default role is CUSTOMER
            Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò ROLE_CUSTOMER"));
            roles.add(customerRole);
        }
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));

        // Check if email is being changed and already exists
        if (!user.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        // Update user fields
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        // Update roles
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : request.getRoles()) {
                try {
                    RoleName roleEnum = RoleName.valueOf(roleName);
                    Role role = roleRepository.findByName(roleEnum)
                            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò: " + roleName));
                    roles.add(role);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Vai trò không hợp lệ: " + roleName);
                }
            }
            user.setRoles(roles);
        }

        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
        userRepository.delete(user);
    }

    @Transactional
    public UserResponse toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
        user.setActive(!user.getActive());
        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác");
        }

        // Verify new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(String roleName) {
        try {
            RoleName roleEnum = RoleName.valueOf(roleName);
            List<User> users = userRepository.findByRoleName(roleEnum, Pageable.unpaged()).getContent();
            return users.stream()
                    .map(UserResponse::fromEntity)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Vai trò không hợp lệ: " + roleName);
        }
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String query, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(query, pageable);
        return users.map(UserResponse::fromEntity);
    }
}
