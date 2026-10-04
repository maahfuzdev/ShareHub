package com.sharehub.sharehub.repository;

import com.sharehub.sharehub.entity.Donation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    @EntityGraph(attributePaths = "donor")
    List<Donation> findByStatusOrderByCreatedAtDesc(Donation.Status status);

    @EntityGraph(attributePaths = "donor")
    List<Donation> findByDonorEmailOrderByCreatedAtDesc(String email);

    @EntityGraph(attributePaths = "donor")
    Optional<Donation> findByIdAndDonorEmail(Long id, String email);
}
