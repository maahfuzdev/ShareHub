// src/main/java/com/sharehub/sharehub/controller/WebController.java

package com.sharehub.sharehub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.sharehub.sharehub.dto.request.RegisterRequest;
import com.sharehub.sharehub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final UserRepository userRepository;

    /**
     * Login Page
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        if (logout != null) {
            model.addAttribute("success", "You have been logged out successfully");
        }
        return "auth/login";
    }

    /**
     * Register Page
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new RegisterRequest());
        return "auth/register";
    }

    /**
     * Dashboard Page (Protected)
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // User info SecurityContext থেকে automatically পাবেন
        return "dashboard/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        userRepository.findByEmail(authentication.getName())
                .ifPresent(user -> model.addAttribute("user", user));
        return "profile/profile";
    }

    @GetMapping("/resources")
    public String resources() {
        return "resources/resources";
    }

    /**
     * Home Page - Redirect to Dashboard
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}
