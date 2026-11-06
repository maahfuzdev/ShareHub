package com.sharehub.sharehub.service;

import com.sharehub.sharehub.entity.Donation;

import java.util.List;

public record AiSearchIntent(
        List<String> keywords,
        Donation.Category category,
        String location
) {
}
