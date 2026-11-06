package com.sharehub.sharehub.controller.web;

import com.sharehub.sharehub.dto.request.DonationRequest;
import com.sharehub.sharehub.entity.Donation;
import com.sharehub.sharehub.entity.DonationImage;
import com.sharehub.sharehub.entity.User;
import com.sharehub.sharehub.service.DonationService;
import com.sharehub.sharehub.service.DonationDiscoveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping({"/resources", "/donations"})
@RequiredArgsConstructor
public class DonationWebController {

    private final DonationService donationService;
    private final DonationDiscoveryService donationDiscoveryService;

    @GetMapping
    public String list(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false, defaultValue = "") String search,
            Model model
    ) {
        DonationDiscoveryService.SearchResult result;
        try {
            result = donationDiscoveryService.search(search);
        } catch (IllegalArgumentException exception) {
            result = donationDiscoveryService.search("");
            model.addAttribute("searchError", exception.getMessage());
        }
        model.addAttribute("donations", result.donations());
        model.addAttribute("search", result.query());
        model.addAttribute("aiSearchStatus", result.status());
        model.addAttribute("availableCount", result.totalAvailable());
        model.addAttribute("isDonor", user.getRole() == User.Role.DONOR);
        if (user.getRole() == User.Role.DONOR) {
            model.addAttribute("myDonations", donationService.findByDonor(user.getEmail()));
        }
        return "resources/resources";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        DonationRequest request = new DonationRequest();
        request.setQuantity("1 item");
        request.setLocation("Local Area");
        model.addAttribute("donation", request);
        model.addAttribute("editingDonation", false);
        return "donations/create";
    }

    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (user.getRole() != User.Role.DONOR) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only donors can edit donation listings.");
            return "redirect:/dashboard";
        }

        return donationService.findOwnedById(id, user.getEmail())
                .map(donation -> {
                    model.addAttribute("donation", toRequest(donation));
                    model.addAttribute("donationId", id);
                    model.addAttribute("editingDonation", true);
                    model.addAttribute("hasImage", donation.isHasImage());
                    return "donations/create";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "That donation was not found in your listings.");
                    return "redirect:/dashboard";
                });
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("donation") DonationRequest request,
            BindingResult bindingResult,
            @RequestParam(name = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal User user,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (user.getRole() != User.Role.DONOR) {
            return "redirect:/dashboard";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("editingDonation", false);
            return "donations/create";
        }

        try {
            donationService.create(request, user.getEmail(), image);
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("image", exception.getMessage());
            model.addAttribute("editingDonation", false);
            return "donations/create";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Your donation has been shared with the community.");
        return "redirect:/dashboard";
    }

    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("donation") DonationRequest request,
            BindingResult bindingResult,
            @RequestParam(name = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal User user,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (user.getRole() != User.Role.DONOR) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only donors can edit donation listings.");
            return "redirect:/dashboard";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("donationId", id);
            model.addAttribute("editingDonation", true);
            donationService.findOwnedById(id, user.getEmail())
                    .ifPresent(donation -> model.addAttribute("hasImage", donation.isHasImage()));
            return "donations/create";
        }

        boolean updated;
        try {
            updated = donationService.update(id, request, user.getEmail(), image);
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("image", exception.getMessage());
            model.addAttribute("donationId", id);
            model.addAttribute("editingDonation", true);
            donationService.findOwnedById(id, user.getEmail())
                    .ifPresent(donation -> model.addAttribute("hasImage", donation.isHasImage()));
            return "donations/create";
        }

        if (updated) {
            redirectAttributes.addFlashAttribute("successMessage", "Your donation details have been updated.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "That donation was not found in your listings.");
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> image(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        return donationService.findImageForUser(id, user.getEmail(), user.getRole())
                .map(this::imageResponse)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/close")
    public String close(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            RedirectAttributes redirectAttributes
    ) {
        if (donationService.close(id, user.getEmail())) {
            redirectAttributes.addFlashAttribute("closed", true);
        } else {
            redirectAttributes.addFlashAttribute("notFound", true);
        }
        return "redirect:/donations";
    }

    @PostMapping("/{id}/availability")
    public String updateAvailability(
            @PathVariable Long id,
            @RequestParam boolean available,
            @AuthenticationPrincipal User user,
            RedirectAttributes redirectAttributes
    ) {
        if (user.getRole() != User.Role.DONOR) {
            redirectAttributes.addFlashAttribute("requestError", "Only donors can update donation availability.");
            return "redirect:/dashboard";
        }

        if (donationService.setAvailability(id, user.getEmail(), available)) {
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    available ? "Your donation is now available to the community."
                            : "Your donation is no longer available to the community."
            );
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "That donation was not found in your listings.");
        }
        return "redirect:/dashboard";
    }

    private DonationRequest toRequest(Donation donation) {
        DonationRequest request = new DonationRequest();
        request.setTitle(donation.getTitle());
        request.setDescription(donation.getDescription());
        request.setCategory(donation.getCategory());
        request.setQuantity(donation.getQuantity());
        request.setLocation(donation.getLocation());
        request.setAvailableUntil(donation.getAvailableUntil());
        return request;
    }

    private ResponseEntity<byte[]> imageResponse(DonationImage image) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .header("X-Content-Type-Options", "nosniff")
                .body(image.getImageData());
    }
}
