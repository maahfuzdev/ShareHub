package com.sharehub.sharehub.controller.web;

import com.sharehub.sharehub.dto.request.DonationRequest;
import com.sharehub.sharehub.entity.Donation;
import com.sharehub.sharehub.entity.User;
import com.sharehub.sharehub.service.DonationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@Controller
@RequestMapping({"/resources", "/donations"})
@RequiredArgsConstructor
public class DonationWebController {

    private final DonationService donationService;

    @GetMapping
    public String list(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("donations", donationService.findAvailable());
        model.addAttribute("isDonor", user.getRole() == User.Role.DONOR);
        if (user.getRole() == User.Role.DONOR) {
            model.addAttribute("myDonations", donationService.findByDonor(user.getEmail()));
        }
        return "resources/resources";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("donation", new DonationRequest());
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
            @AuthenticationPrincipal User user,
            Model model
    ) {
        if (user.getRole() != User.Role.DONOR) {
            return "redirect:/dashboard";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("editingDonation", false);
            return "donations/create";
        }

        donationService.create(request, user.getEmail());
        return "redirect:/donations?created";
    }

    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("donation") DonationRequest request,
            BindingResult bindingResult,
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
            return "donations/create";
        }

        if (donationService.update(id, request, user.getEmail())) {
            redirectAttributes.addFlashAttribute("successMessage", "Your donation details have been updated.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "That donation was not found in your listings.");
        }
        return "redirect:/dashboard";
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
}
