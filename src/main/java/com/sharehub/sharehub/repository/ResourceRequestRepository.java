package com.sharehub.sharehub.repository;

import com.sharehub.sharehub.entity.ResourceRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceRequestRepository extends JpaRepository<ResourceRequest, Long> {

    @EntityGraph(attributePaths = "donation")
    List<ResourceRequest> findByRecipientEmailOrderByCreatedAtDesc(String email);
}
