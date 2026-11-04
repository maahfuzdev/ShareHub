// src/main/java/com/sharehub/repository/UserRepository.java

package com.sharehub.sharehub.repository;

import com.sharehub.sharehub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}