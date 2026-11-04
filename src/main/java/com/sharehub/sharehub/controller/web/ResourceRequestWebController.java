package com.sharehub.sharehub.controller.web;

import com.sharehub.sharehub.entity.User;
import com.sharehub.sharehub.service.ResourceRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/recipient/requests")
@RequiredArgsConstructor
public class ResourceRequestWebController {

    private final ResourceRequestService resourceRequestService;

    @PostMapping
    public String create(
            @RequestParam Long donationId,
            @RequestParam(required = false) String message,
            @AuthenticationPrincipal User recipient,
            RedirectAttributes redirectAttributes
    ) {
        if (recipient.getRole() != User.Role.RECIPIENT) {
            redirectAttributes.addFlashAttribute("requestError", "Only recipients can request donations.");
            return "redirect:/dashboard";
        }

        try {
            resourceRequestService.create(donationId, message, recipient.getEmail());
            redirectAttributes.addFlashAttribute("requestSuccess", "Your request has been submitted.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("requestError", exception.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/{requestId}/cancel")
    public String cancel(
            @PathVariable Long requestId,
            @AuthenticationPrincipal User recipient,
            RedirectAttributes redirectAttributes
    ) {
        if (recipient.getRole() != User.Role.RECIPIENT) {
            redirectAttributes.addFlashAttribute("requestError", "Only recipients can cancel requests.");
            return "redirect:/dashboard";
        }

        if (resourceRequestService.cancel(requestId, recipient.getEmail())) {
            redirectAttributes.addFlashAttribute("requestSuccess", "Your request has been cancelled.");
        } else {
            redirectAttributes.addFlashAttribute(
                    "requestError",
                    "That request could not be cancelled. It may no longer be pending."
            );
        }
        return "redirect:/dashboard";
    }
}
