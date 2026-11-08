package com.sharehub.sharehub.repository;

import com.sharehub.sharehub.entity.Donation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    @EntityGraph(attributePaths = "donor")
    List<Donation> findByStatusAndOriginOrderByCreatedAtDesc(Donation.Status status, Donation.Origin origin);

    @EntityGraph(attributePaths = "donor")
    List<Donation> findByDonorEmailAndOriginOrderByCreatedAtDesc(String email, Donation.Origin origin);

    @EntityGraph(attributePaths = "donor")
    Optional<Donation> findByIdAndDonorEmail(Long id, String email);

    Optional<Donation> findByIdAndStatus(Long id, Donation.Status status);
}
