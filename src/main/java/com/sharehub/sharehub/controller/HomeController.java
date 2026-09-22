package com.sharehub.sharehub.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HomeController {

    /**
     * Protected endpoint - শুধু logged in user access করতে পারবে
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication != null && authentication.isAuthenticated()) {
            response.put("message", "Welcome to your dashboard!");
            response.put("email", authentication.getName());
            response.put("authorities", authentication.getAuthorities());
            response.put("authenticated", true);
        } else {
            response.put("message", "Not authenticated");
            response.put("authenticated", false);
        }

        return ResponseEntity.ok(response);
    }
}