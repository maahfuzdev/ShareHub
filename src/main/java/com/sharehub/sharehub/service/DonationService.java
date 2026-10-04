package com.sharehub.sharehub.service;

import com.sharehub.sharehub.dto.request.DonationRequest;
import com.sharehub.sharehub.entity.Donation;
import com.sharehub.sharehub.entity.User;
import com.sharehub.sharehub.repository.DonationRepository;
import com.sharehub.sharehub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationRepository donationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Donation> findAvailable() {
        return donationRepository.findByStatusOrderByCreatedAtDesc(Donation.Status.AVAILABLE);
    }

    @Transactional(readOnly = true)
    public List<Donation> findByDonor(String email) {
        return donationRepository.findByDonorEmailOrderByCreatedAtDesc(email);
    }

    @Transactional
    public Donation create(DonationRequest request, String donorEmail) {
        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated donor was not found"));

        Donation donation = new Donation();
        donation.setTitle(request.getTitle().trim());
        donation.setDescription(request.getDescription().trim());
        donation.setCategory(request.getCategory());
        donation.setQuantity(request.getQuantity().trim());
        donation.setLocation(request.getLocation().trim());
        donation.setAvailableUntil(request.getAvailableUntil());
        donation.setStatus(Donation.Status.AVAILABLE);
        donation.setDonor(donor);
        return donationRepository.save(donation);
    }

    @Transactional
    public boolean close(Long id, String donorEmail) {
        return donationRepository.findByIdAndDonorEmail(id, donorEmail)
                .map(donation -> {
                    donation.setStatus(Donation.Status.CLOSED);
                    donationRepository.save(donation);
                    return true;
                })
                .orElse(false);
    }
}
