package com.example.backend_quanlynhahanglau.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {
    @NotBlank(message = "Tên đầy đủ không được để trống")
    @Size(max = 100, message = "Tên đầy đủ không được vượt quá 100 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @Size(max = 15, message = "Số điện thoại không được vượt quá 15 ký tự")
    private String phoneNumber;

    private Set<String> roles;

    private String address;

    private Boolean active;
}
