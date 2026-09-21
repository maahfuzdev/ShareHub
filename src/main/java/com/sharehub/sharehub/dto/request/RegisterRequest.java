// src/main/java/com/sharehub/dto/request/RegisterRequest.java

package com.sharehub.sharehub.dto.request;

import com.sharehub.sharehub.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Name is required")
    private String name;

    private String phone;

    private String address;

    private Double latitude;

    private Double longitude;

    @NotNull(message = "Role is required (DONOR or RECIPIENT)")
    private User.Role role;
}