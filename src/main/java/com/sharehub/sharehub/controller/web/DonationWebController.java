package com.sharehub.sharehub.controller.web;

import com.sharehub.sharehub.dto.request.DonationRequest;
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
        return "donations/create";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("donation") DonationRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal User user
    ) {
        if (bindingResult.hasErrors()) {
            return "donations/create";
        }

        donationService.create(request, user.getEmail());
        return "redirect:/donations?created";
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
}
