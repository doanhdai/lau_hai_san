package com.example.backend_quanlynhahanglau.controller;

import com.example.backend_quanlynhahanglau.dto.ApiResponse;
import com.example.backend_quanlynhahanglau.dto.auth.JwtResponse;
import com.example.backend_quanlynhahanglau.dto.auth.LoginRequest;
import com.example.backend_quanlynhahanglau.dto.auth.RegisterRequest;
import com.example.backend_quanlynhahanglau.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        String message = authService.register(registerRequest);
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }
}
