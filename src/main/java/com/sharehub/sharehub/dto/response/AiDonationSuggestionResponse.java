package com.sharehub.sharehub.dto.response;

import com.sharehub.sharehub.entity.Donation;

import java.util.List;

public record AiDonationSuggestionResponse(
        String title,
        String description,
        Donation.Category category,
        List<String> missingDetails
) {
}
