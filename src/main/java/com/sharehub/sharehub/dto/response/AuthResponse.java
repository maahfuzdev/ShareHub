// src/main/java/com/sharehub/dto/response/AuthResponse.java

package com.sharehub.sharehub.dto.response;

import com.sharehub.sharehub.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String message;
    private Long userId;
    private String email;
    private String name;
    private User.Role role;
    private String sessionId;

    // Login Success Constructor
    public AuthResponse(String message, Long userId, String email,
                        String name, User.Role role) {
        this.message = message;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
    }
}