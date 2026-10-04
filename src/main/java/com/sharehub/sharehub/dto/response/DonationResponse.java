package com.sharehub.sharehub.dto.response;

import com.sharehub.sharehub.entity.Donation;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DonationResponse(
        Long id,
        String title,
        String description,
        Donation.Category category,
        String quantity,
        String location,
        LocalDate availableUntil,
        Donation.Status status,
        String donorName,
        LocalDateTime createdAt
) {
    public static DonationResponse from(Donation donation) {
        return new DonationResponse(
                donation.getId(),
                donation.getTitle(),
                donation.getDescription(),
                donation.getCategory(),
                donation.getQuantity(),
                donation.getLocation(),
                donation.getAvailableUntil(),
                donation.getStatus(),
                donation.getDonor().getName(),
                donation.getCreatedAt()
        );
    }
}
