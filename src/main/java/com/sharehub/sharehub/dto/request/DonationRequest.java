package com.sharehub.sharehub.dto.request;

import com.sharehub.sharehub.entity.Donation;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class DonationRequest {

    @NotBlank
    @Size(max = 120)
    private String title;

    @NotBlank
    @Size(max = 2000)
    private String description;

    @NotNull
    private Donation.Category category;

    @NotBlank
    @Size(max = 80)
    private String quantity;

    @NotBlank
    @Size(max = 160)
    private String location;

    @FutureOrPresent
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate availableUntil;
}
