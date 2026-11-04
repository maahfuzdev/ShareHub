// src/main/java/com/sharehub/sharehub/controller/WebController.java

package com.sharehub.sharehub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.sharehub.sharehub.entity.ResourceRequest;
import com.sharehub.sharehub.entity.User;
import com.sharehub.sharehub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.sharehub.sharehub.service.DonationService;
import com.sharehub.sharehub.service.ResourceRequestService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final UserRepository userRepository;
    private final DonationService donationService;
    private final ResourceRequestService resourceRequestService;

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
    public String dashboard(@AuthenticationPrincipal User user, Model model) {
        if (user.getRole() == User.Role.RECIPIENT) {
            List<ResourceRequest> requests = resourceRequestService.findByRecipient(user.getEmail());
            var availableDonations = donationService.findAvailable();
            model.addAttribute("availableDonations", availableDonations);
            model.addAttribute("recipientRequests", requests);
            model.addAttribute("availableItemsCount", availableDonations.size());
            model.addAttribute("activeRequestsCount", requests.stream()
                    .filter(request -> request.getStatus() == ResourceRequest.Status.PENDING
                            || request.getStatus() == ResourceRequest.Status.APPROVED)
                    .count());
            model.addAttribute("itemsReceivedCount", requests.stream()
                    .filter(request -> request.getStatus() == ResourceRequest.Status.FULFILLED)
                    .count());
            model.addAttribute("myRequestsCount", requests.size());
        }
        return "dashboard/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        userRepository.findByEmail(authentication.getName())
                .ifPresent(user -> model.addAttribute("user", user));
        return "profile/profile";
    }

    /**
     * Home Page - Redirect to Dashboard
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}
