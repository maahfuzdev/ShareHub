package com.sharehub.sharehub.service;

import com.sharehub.sharehub.dto.request.DonationRequest;
import com.sharehub.sharehub.entity.Donation;
import com.sharehub.sharehub.entity.User;
import com.sharehub.sharehub.repository.DonationRepository;
import com.sharehub.sharehub.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DonationServiceTest {

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DonationService donationService;

    @Test
    void createAssociatesListingWithAuthenticatedDonorAndStartsAvailable() {
        User donor = new User();
        donor.setEmail("donor@example.com");
        donor.setRole(User.Role.DONOR);

        DonationRequest request = new DonationRequest();
        request.setTitle("  Fresh produce  ");
        request.setDescription("  Vegetables for a neighbor  ");
        request.setCategory(Donation.Category.FOOD);
        request.setQuantity("  2 boxes  ");
        request.setLocation("  Dhanmondi  ");

        when(userRepository.findByEmail("donor@example.com")).thenReturn(Optional.of(donor));
        when(donationRepository.save(any(Donation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Donation result = donationService.create(request, "donor@example.com");

        assertEquals("Fresh produce", result.getTitle());
        assertEquals("Vegetables for a neighbor", result.getDescription());
        assertEquals("2 boxes", result.getQuantity());
        assertEquals(Donation.Status.AVAILABLE, result.getStatus());
        assertEquals(donor, result.getDonor());
    }

    @Test
    void closeOnlyUpdatesAListingOwnedByTheAuthenticatedDonor() {
        Donation donation = new Donation();
        when(donationRepository.findByIdAndDonorEmail(7L, "owner@example.com"))
                .thenReturn(Optional.of(donation));

        assertTrue(donationService.close(7L, "owner@example.com"));
        assertEquals(Donation.Status.CLOSED, donation.getStatus());
        verify(donationRepository).save(donation);
    }

    @Test
    void closeDoesNotChangeAListingOwnedBySomeoneElse() {
        when(donationRepository.findByIdAndDonorEmail(7L, "other@example.com"))
                .thenReturn(Optional.empty());

        assertFalse(donationService.close(7L, "other@example.com"));
        verify(donationRepository, never()).save(any(Donation.class));
    }
}
