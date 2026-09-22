package com.sharehub.sharehub.controller;

import com.sharehub.sharehub.dto.request.LoginRequest;
import com.sharehub.sharehub.dto.request.RegisterRequest;
import com.sharehub.sharehub.dto.response.AuthResponse;
import com.sharehub.sharehub.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, Login & Logout APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user (DONOR or RECIPIENT)")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.register(request, httpRequest));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate session")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest httpRequest) {
        authService.logout(httpRequest);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/session-info")
    @Operation(summary = "Get current session information")
    public ResponseEntity<Map<String, Object>> getSessionInfo(HttpServletRequest request) {
        Map<String, Object> info = new HashMap<>();
        var session = request.getSession(false);
        if (session == null) {
            info.put("authenticated", false);
            info.put("message", "No active session");
        } else {
            info.put("authenticated", true);
            info.put("sessionId", session.getId());
            info.put("creationTime", session.getCreationTime());
            info.put("lastAccessedTime", session.getLastAccessedTime());
            info.put("maxInactiveInterval", session.getMaxInactiveInterval());
        }
        return ResponseEntity.ok(info);
    }
}