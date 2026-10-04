package com.sharehub.sharehub.controller.api;

import com.sharehub.sharehub.dto.request.DonationRequest;
import com.sharehub.sharehub.dto.response.DonationResponse;
import com.sharehub.sharehub.service.DonationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
@Tag(name = "Donations", description = "Browse and manage community donation listings")
public class DonationApiController {

    private final DonationService donationService;

    @GetMapping
    @Operation(summary = "List available donations")
    public List<DonationResponse> listAvailable() {
        return donationService.findAvailable().stream()
                .map(DonationResponse::from)
                .toList();
    }

    @GetMapping("/mine")
    @Operation(summary = "List the authenticated donor's donations")
    public List<DonationResponse> listMine(Authentication authentication) {
        return donationService.findByDonor(authentication.getName()).stream()
                .map(DonationResponse::from)
                .toList();
    }

    @PostMapping
    @Operation(summary = "Create a donation listing (donors only)")
    public ResponseEntity<DonationResponse> create(
            @Valid @RequestBody DonationRequest request,
            Authentication authentication
    ) {
        DonationResponse response = DonationResponse.from(
                donationService.create(request, authentication.getName())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(path = "/{id}/close", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Close one of the authenticated donor's listings")
    public ResponseEntity<Void> close(@PathVariable Long id, Authentication authentication) {
        if (!donationService.close(id, authentication.getName())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
