package com.sharehub.sharehub.service;

import com.sharehub.sharehub.entity.Donation;
import com.sharehub.sharehub.entity.ResourceRequest;
import com.sharehub.sharehub.entity.User;
import com.sharehub.sharehub.repository.DonationRepository;
import com.sharehub.sharehub.repository.ResourceRequestRepository;
import com.sharehub.sharehub.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceRequestServiceTest {

    @Mock
    private ResourceRequestRepository resourceRequestRepository;

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ResourceRequestService resourceRequestService;

    @Test
    void createSavesPendingRequestForAvailableDonation() {
        User recipient = new User();
        recipient.setEmail("recipient@example.com");
        recipient.setRole(User.Role.RECIPIENT);

        Donation donation = new Donation();
        donation.setId(5L);

        when(userRepository.findByEmail(recipient.getEmail())).thenReturn(Optional.of(recipient));
        when(donationRepository.findById(5L)).thenReturn(Optional.of(donation));
        when(resourceRequestRepository.findByRecipientEmailOrderByCreatedAtDesc(recipient.getEmail()))
                .thenReturn(List.of());
        when(resourceRequestRepository.save(any(ResourceRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResourceRequest result = resourceRequestService.create(5L, "  I can pick it up today  ", recipient.getEmail());

        assertEquals(donation, result.getDonation());
        assertEquals(recipient, result.getRecipient());
        assertEquals("I can pick it up today", result.getMessage());
        assertEquals(ResourceRequest.Status.PENDING, result.getStatus());
        verify(resourceRequestRepository).save(result);
    }

    @Test
    void createRejectsUnavailableDonation() {
        User recipient = new User();
        recipient.setEmail("recipient@example.com");
        recipient.setRole(User.Role.RECIPIENT);

        Donation donation = new Donation();
        donation.setStatus(Donation.Status.CLOSED);

        when(userRepository.findByEmail(recipient.getEmail())).thenReturn(Optional.of(recipient));
        when(donationRepository.findById(5L)).thenReturn(Optional.of(donation));

        assertThrows(IllegalStateException.class,
                () -> resourceRequestService.create(5L, null, recipient.getEmail()));
        verify(resourceRequestRepository, never()).save(any(ResourceRequest.class));
    }

    @Test
    void createRejectsAnotherActiveRequestForSameDonation() {
        User recipient = new User();
        recipient.setEmail("recipient@example.com");
        recipient.setRole(User.Role.RECIPIENT);

        Donation donation = new Donation();
        donation.setId(5L);

        ResourceRequest existingRequest = new ResourceRequest();
        existingRequest.setDonation(donation);
        existingRequest.setStatus(ResourceRequest.Status.PENDING);

        when(userRepository.findByEmail(recipient.getEmail())).thenReturn(Optional.of(recipient));
        when(donationRepository.findById(5L)).thenReturn(Optional.of(donation));
        when(resourceRequestRepository.findByRecipientEmailOrderByCreatedAtDesc(recipient.getEmail()))
                .thenReturn(List.of(existingRequest));

        assertThrows(IllegalStateException.class,
                () -> resourceRequestService.create(5L, null, recipient.getEmail()));
        verify(resourceRequestRepository, never()).save(any(ResourceRequest.class));
    }

    @Test
    void createRejectsNonRecipientUser() {
        User donor = new User();
        donor.setEmail("donor@example.com");
        donor.setRole(User.Role.DONOR);
        when(userRepository.findByEmail(donor.getEmail())).thenReturn(Optional.of(donor));

        assertThrows(IllegalStateException.class,
                () -> resourceRequestService.create(5L, null, donor.getEmail()));
        verify(donationRepository, never()).findById(5L);
    }

    @Test
    void cancelUpdatesOnlyPendingRequestOwnedByRecipient() {
        User recipient = new User();
        recipient.setEmail("recipient@example.com");

        ResourceRequest request = new ResourceRequest();
        request.setRecipient(recipient);
        request.setStatus(ResourceRequest.Status.PENDING);
        when(resourceRequestRepository.findById(9L)).thenReturn(Optional.of(request));

        assertTrue(resourceRequestService.cancel(9L, recipient.getEmail()));
        assertEquals(ResourceRequest.Status.CANCELLED, request.getStatus());
    }

    @Test
    void cancelRejectsRequestsNotOwnedByRecipientOrNoLongerPending() {
        User owner = new User();
        owner.setEmail("owner@example.com");
        ResourceRequest request = new ResourceRequest();
        request.setRecipient(owner);
        request.setStatus(ResourceRequest.Status.APPROVED);
        when(resourceRequestRepository.findById(9L)).thenReturn(Optional.of(request));

        assertFalse(resourceRequestService.cancel(9L, "other@example.com"));
        assertEquals(ResourceRequest.Status.APPROVED, request.getStatus());
        assertFalse(resourceRequestService.cancel(9L, owner.getEmail()));
    }
}
