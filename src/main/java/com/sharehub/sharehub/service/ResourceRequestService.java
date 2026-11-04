package com.sharehub.sharehub.service;

import com.sharehub.sharehub.entity.Donation;
import com.sharehub.sharehub.entity.ResourceRequest;
import com.sharehub.sharehub.entity.User;
import com.sharehub.sharehub.repository.DonationRepository;
import com.sharehub.sharehub.repository.ResourceRequestRepository;
import com.sharehub.sharehub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceRequestService {

    private static final int MAX_MESSAGE_LENGTH = 1000;

    private final ResourceRequestRepository resourceRequestRepository;
    private final DonationRepository donationRepository;
    private final UserRepository userRepository;

    @Transactional
    public ResourceRequest create(Long donationId, String message, String recipientEmail) {
        String normalizedMessage = message == null || message.isBlank() ? null : message.trim();
        if (normalizedMessage != null && normalizedMessage.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Request message must be 1000 characters or fewer");
        }

        User recipient = userRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated recipient was not found"));
        if (recipient.getRole() != User.Role.RECIPIENT) {
            throw new IllegalStateException("Only recipients can request donations");
        }

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Donation was not found"));
        if (donation.getStatus() != Donation.Status.AVAILABLE) {
            throw new IllegalStateException("This donation is no longer available");
        }

        boolean alreadyActive = resourceRequestRepository
                .findByRecipientEmailOrderByCreatedAtDesc(recipientEmail)
                .stream()
                .anyMatch(existing -> existing.getDonation().getId().equals(donationId)
                        && (existing.getStatus() == ResourceRequest.Status.PENDING
                        || existing.getStatus() == ResourceRequest.Status.APPROVED));
        if (alreadyActive) {
            throw new IllegalStateException("You already have an active request for this donation");
        }

        ResourceRequest request = new ResourceRequest();
        request.setDonation(donation);
        request.setRecipient(recipient);
        request.setMessage(normalizedMessage);
        request.setStatus(ResourceRequest.Status.PENDING);
        return resourceRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<ResourceRequest> findByRecipient(String recipientEmail) {
        return resourceRequestRepository.findByRecipientEmailOrderByCreatedAtDesc(recipientEmail);
    }

    @Transactional
    public boolean cancel(Long requestId, String recipientEmail) {
        return resourceRequestRepository.findById(requestId)
                .filter(request -> request.getRecipient().getEmail().equals(recipientEmail))
                .filter(request -> request.getStatus() == ResourceRequest.Status.PENDING)
                .map(request -> {
                    request.setStatus(ResourceRequest.Status.CANCELLED);
                    return true;
                })
                .orElse(false);
    }
}
