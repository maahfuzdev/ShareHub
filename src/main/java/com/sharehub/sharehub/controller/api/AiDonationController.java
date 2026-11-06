package com.sharehub.sharehub.controller.api;

import com.sharehub.sharehub.dto.request.AiDonationSuggestionRequest;
import com.sharehub.sharehub.dto.response.AiDonationSuggestionResponse;
import com.sharehub.sharehub.entity.User;
import com.sharehub.sharehub.service.AiAssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/donations/ai", "/api/donations/ai"})
@RequiredArgsConstructor
public class AiDonationController {

    private final AiAssistantService aiAssistantService;

    @PostMapping("/suggest")
    public ResponseEntity<AiDonationSuggestionResponse> suggest(
            @Valid @RequestBody AiDonationSuggestionRequest request,
            @AuthenticationPrincipal User user
    ) {
        if (user == null || user.getRole() != User.Role.DONOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(aiAssistantService.suggestDonation(request.getNotes().trim()));
    }
}
